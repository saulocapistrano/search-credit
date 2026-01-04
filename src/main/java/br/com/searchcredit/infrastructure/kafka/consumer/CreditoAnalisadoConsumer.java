package br.com.searchcredit.infrastructure.kafka.consumer;

import br.com.searchcredit.domain.entity.Credito;
import br.com.searchcredit.domain.entity.CreditoAnaliseAutomatica;
import br.com.searchcredit.domain.enums.StatusCredito;
import br.com.searchcredit.domain.repository.CreditoRepository;
import br.com.searchcredit.infrastructure.kafka.event.CreditoAnalisadoEvent;
import br.com.searchcredit.infrastructure.repository.jpa.CreditoAnaliseAutomaticaJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Component
public class CreditoAnalisadoConsumer {

    public static final String TOPIC_CREDITO_ANALISADO = "credito-analisado-topic";

    private final CreditoRepository creditoRepository;
    private final CreditoAnaliseAutomaticaJpaRepository creditoAnaliseAutomaticaJpaRepository;

    public CreditoAnalisadoConsumer(
            CreditoRepository creditoRepository,
            CreditoAnaliseAutomaticaJpaRepository creditoAnaliseAutomaticaJpaRepository) {
        this.creditoRepository = creditoRepository;
        this.creditoAnaliseAutomaticaJpaRepository = creditoAnaliseAutomaticaJpaRepository;
    }

    @Transactional
    @KafkaListener(
            topics = TOPIC_CREDITO_ANALISADO,
            containerFactory = "creditoAnalisadoKafkaListenerContainerFactory")
    public void onMessage(CreditoAnalisadoEvent event) {
        if (event == null || event.getNumeroCredito() == null || event.getNumeroCredito().isBlank()) {
            log.warn("Evento CreditoAnalisadoEvent inválido recebido (numeroCredito ausente). Ignorando.");
            return;
        }

        String numeroCredito = event.getNumeroCredito();

        if (creditoAnaliseAutomaticaJpaRepository.existsByNumeroCredito(numeroCredito)) {
            log.info("Evento duplicado ignorado. numeroCredito={}", numeroCredito);
            return;
        }

        Optional<Credito> creditoOpt = creditoRepository.findByNumeroCredito(numeroCredito);

        if (creditoOpt.isEmpty()) {
            log.warn("CreditoAnalisadoEvent recebido para numeroCredito={} mas crédito não existe. Ignorando.", numeroCredito);
            return;
        }

        Credito credito = creditoOpt.get();

        if (credito.getStatus() != StatusCredito.EM_ANALISE) {
            log.info("Evento ignorado — crédito já analisado manualmente. numeroCredito={}, statusAtual={}",
                    numeroCredito, credito.getStatus());
            return;
        }

        if (event.getStatus() != StatusCredito.APROVADO && event.getStatus() != StatusCredito.REPROVADO) {
            log.warn("CreditoAnalisadoEvent recebido com status inválido. numeroCredito={}, statusEvento={}. Ignorando.",
                    numeroCredito, event.getStatus());
            return;
        }

        credito.setStatus(event.getStatus());
        credito.setAprovadoPor("AUTO_WORKER");
        credito.setComentarioAnalise("Análise automática simulada");
        credito.setDataAnalise(LocalDateTime.now());

        creditoRepository.save(credito);

        creditoAnaliseAutomaticaJpaRepository.save(CreditoAnaliseAutomatica.builder()
                .numeroCredito(numeroCredito)
                .dataProcessamento(LocalDateTime.now())
                .build());

        log.info("Análise automática aplicada com sucesso. numeroCredito={}, novoStatus={}", numeroCredito, event.getStatus());
    }
}
