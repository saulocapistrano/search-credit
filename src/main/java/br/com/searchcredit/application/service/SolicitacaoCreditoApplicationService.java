package br.com.searchcredit.application.service;

import br.com.searchcredit.domain.entity.SolicitacaoCredito;
import br.com.searchcredit.domain.enums.StatusSolicitacao;
import br.com.searchcredit.domain.repository.SolicitacaoCreditoRepository;
import br.com.searchcredit.domain.service.AnaliseSolicitacaoCreditoService;
import br.com.searchcredit.infrastructure.kafka.KafkaEventPublisher;
import br.com.searchcredit.infrastructure.kafka.event.SolicitacaoCreditoAnalisadaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application Service responsável por orquestrar a análise de solicitações de crédito.
 * 
 * Responsabilidades:
 * - Buscar solicitação
 * - Delegar regras de negócio para o Domain Service
 * - Persistir alterações
 * - Publicar eventos Kafka
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SolicitacaoCreditoApplicationService {

    private final SolicitacaoCreditoRepository repository;
    private final AnaliseSolicitacaoCreditoService analiseService;
    private final KafkaEventPublisher kafkaEventPublisher;

    /**
     * Responde à análise de uma solicitação de crédito.
     * 
     * @param solicitacaoId ID da solicitação a ser analisada
     * @param status Novo status (APROVADO ou REPROVADO)
     * @param comentario Comentário da análise
     * @throws IllegalArgumentException Se a solicitação não for encontrada
     * @throws IllegalStateException Se a solicitação não estiver em EM_ANALISE
     */
    @Transactional
    public void responderAnalise(Long solicitacaoId, StatusSolicitacao status, String comentario) {
        log.info("Iniciando análise da solicitação ID: {}", solicitacaoId);

        // 1. Buscar solicitação pelo ID
        SolicitacaoCredito solicitacao = repository.findById(solicitacaoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Solicitação com ID %d não encontrada", solicitacaoId)
                ));

        // 2. Delegar regra de negócio para o Domain Service
        analiseService.analisarSolicitacao(solicitacao, status, comentario);

        // 3. Salvar no repositório
        SolicitacaoCredito solicitacaoAtualizada = repository.save(solicitacao);
        log.info("Solicitação ID: {} atualizada com status: {}", solicitacaoId, status);

        // 4. Publicar evento Kafka
        SolicitacaoCreditoAnalisadaEvent event = new SolicitacaoCreditoAnalisadaEvent(
                solicitacaoAtualizada.getId(),
                solicitacaoAtualizada.getNumeroCredito(),
                solicitacaoAtualizada.getNomeSolicitante(),
                solicitacaoAtualizada.getStatus(),
                solicitacaoAtualizada.getDataAnalise(),
                solicitacaoAtualizada.getComentarioAnalise()
        );

        kafkaEventPublisher.publishSolicitacaoAnalisada(event);
        log.info("Evento de análise publicado para solicitação ID: {}", solicitacaoId);
    }
}

