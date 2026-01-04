package br.com.searchcredit.application.service;

import br.com.searchcredit.domain.entity.Credito;
import br.com.searchcredit.domain.enums.StatusCredito;
import br.com.searchcredit.domain.repository.CreditoRepository;
import br.com.searchcredit.domain.service.AnaliseSolicitacaoCreditoService;
import br.com.searchcredit.infrastructure.kafka.KafkaEventPublisher;
import br.com.searchcredit.infrastructure.kafka.event.SolicitacaoCreditoAnalisadaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application Service responsável por orquestrar a análise de créditos.
 * 
 * Responsabilidades:
 * - Buscar crédito
 * - Delegar regras de negócio para o Domain Service
 * - Persistir alterações
 * - Publicar eventos Kafka
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SolicitacaoCreditoApplicationService {

    private final CreditoRepository creditoRepository;
    private final AnaliseSolicitacaoCreditoService analiseService;
    private final KafkaEventPublisher kafkaEventPublisher;

    /**
     * Responde à análise de um crédito.
     * 
     * @param creditoId ID do crédito a ser analisado
     * @param status Novo status (APROVADO ou REPROVADO)
     * @param comentario Comentário da análise
     * @throws IllegalArgumentException Se o crédito não for encontrado
     * @throws IllegalStateException Se o crédito não estiver em EM_ANALISE
     */
    @Transactional
    public void responderAnalise(Long creditoId, StatusCredito status, String comentario) {
        log.info("Iniciando análise do crédito ID: {}", creditoId);

        // 1. Buscar crédito pelo ID
        Credito credito = creditoRepository.findById(creditoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Crédito com ID %d não encontrado", creditoId)
                ));

        // 2. Delegar regra de negócio para o Domain Service
        analiseService.analisarCredito(credito, status, comentario);

        // 3. Salvar no repositório
        Credito creditoAtualizado = creditoRepository.save(credito);
        log.info("Crédito ID: {} atualizado com status: {}", creditoId, status);

        // 4. Publicar eventos Kafka (mantém compatibilidade com evento legado + novo evento de domínio)
        
        // 4.1. Publicar evento legado (compatibilidade com consumidores existentes)
        // Converter StatusCredito para StatusSolicitacao apenas para o evento legado
        br.com.searchcredit.domain.enums.StatusSolicitacao statusLegado = convertStatusCreditoToStatusSolicitacao(status);
        SolicitacaoCreditoAnalisadaEvent eventLegado = new SolicitacaoCreditoAnalisadaEvent(
                creditoAtualizado.getId(),
                creditoAtualizado.getNumeroCredito(),
                statusLegado, // Conversão apenas para compatibilidade com evento Kafka legado
                creditoAtualizado.getDataAnalise(),
                creditoAtualizado.getComentarioAnalise()
        );
        kafkaEventPublisher.publishSolicitacaoAnalisada(eventLegado);
        log.info("SolicitacaoCreditoAnalisadaEvent (legado) publicado para crédito ID: {}", creditoId);

        // 4.2. Publicar novo evento de domínio CreditoAnalisadoEvent
        kafkaEventPublisher.publishCreditoAnalisado(creditoAtualizado);
        log.info("CreditoAnalisadoEvent (novo) publicado para crédito ID: {}", creditoId);
    }

    /**
     * Converte StatusCredito para StatusSolicitacao (apenas para compatibilidade com evento Kafka legado).
     */
    private br.com.searchcredit.domain.enums.StatusSolicitacao convertStatusCreditoToStatusSolicitacao(StatusCredito statusCredito) {
        if (statusCredito == null) {
            return null;
        }

        return switch (statusCredito) {
            case EM_ANALISE -> br.com.searchcredit.domain.enums.StatusSolicitacao.EM_ANALISE;
            case APROVADO -> br.com.searchcredit.domain.enums.StatusSolicitacao.APROVADO;
            case REPROVADO -> br.com.searchcredit.domain.enums.StatusSolicitacao.REPROVADO;
            case CONSTITUIDO -> br.com.searchcredit.domain.enums.StatusSolicitacao.EM_ANALISE; // Fallback
        };
    }
}

