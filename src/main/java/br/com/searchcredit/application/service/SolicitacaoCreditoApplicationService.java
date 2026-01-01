package br.com.searchcredit.application.service;

import br.com.searchcredit.domain.entity.Credito;
import br.com.searchcredit.domain.enums.StatusCredito;
import br.com.searchcredit.domain.enums.StatusSolicitacao;
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
     * @param status Novo status (APROVADO ou REPROVADO) - StatusSolicitacao para compatibilidade
     * @param comentario Comentário da análise
     * @throws IllegalArgumentException Se o crédito não for encontrado
     * @throws IllegalStateException Se o crédito não estiver em EM_ANALISE
     */
    @Transactional
    public void responderAnalise(Long creditoId, StatusSolicitacao status, String comentario) {
        log.info("Iniciando análise do crédito ID: {}", creditoId);

        // 1. Buscar crédito pelo ID
        Credito credito = creditoRepository.findById(creditoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Crédito com ID %d não encontrado", creditoId)
                ));

        // 2. Converter StatusSolicitacao para StatusCredito
        StatusCredito statusCredito = convertStatusSolicitacaoToStatusCredito(status);

        // 3. Delegar regra de negócio para o Domain Service
        analiseService.analisarCredito(credito, statusCredito, comentario);

        // 4. Salvar no repositório
        Credito creditoAtualizado = creditoRepository.save(credito);
        log.info("Crédito ID: {} atualizado com status: {}", creditoId, statusCredito);

        // 5. Publicar evento Kafka (mantém compatibilidade com evento existente)
        SolicitacaoCreditoAnalisadaEvent event = new SolicitacaoCreditoAnalisadaEvent(
                creditoAtualizado.getId(),
                creditoAtualizado.getNumeroCredito(),
                creditoAtualizado.getNomeSolicitante(),
                status, // Mantém StatusSolicitacao para compatibilidade com evento Kafka
                creditoAtualizado.getDataAnalise(),
                creditoAtualizado.getComentarioAnalise()
        );

        kafkaEventPublisher.publishSolicitacaoAnalisada(event);
        log.info("Evento de análise publicado para crédito ID: {}", creditoId);
    }

    /**
     * Converte StatusSolicitacao para StatusCredito.
     */
    private StatusCredito convertStatusSolicitacaoToStatusCredito(StatusSolicitacao statusSolicitacao) {
        if (statusSolicitacao == null) {
            return null;
        }

        return switch (statusSolicitacao) {
            case EM_ANALISE -> StatusCredito.EM_ANALISE;
            case APROVADO -> StatusCredito.APROVADO;
            case REPROVADO -> StatusCredito.REPROVADO;
        };
    }
}

