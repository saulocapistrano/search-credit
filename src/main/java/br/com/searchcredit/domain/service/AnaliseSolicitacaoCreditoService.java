package br.com.searchcredit.domain.service;

import br.com.searchcredit.domain.entity.SolicitacaoCredito;
import br.com.searchcredit.domain.enums.StatusSolicitacao;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Serviço de domínio responsável pelas regras de negócio relacionadas à análise de solicitações de crédito.
 * 
 * Este serviço contém APENAS regras de domínio, sem dependências de infraestrutura.
 */
@Component
public class AnaliseSolicitacaoCreditoService {

    /**
     * Analisa uma solicitação de crédito, aplicando as regras de negócio.
     * 
     * @param solicitacao A solicitação a ser analisada
     * @param novoStatus O novo status a ser aplicado (APROVADO ou REPROVADO)
     * @param comentario O comentário da análise
     * @throws IllegalStateException Se a solicitação não estiver em EM_ANALISE
     */
    public void analisarSolicitacao(
            SolicitacaoCredito solicitacao,
            StatusSolicitacao novoStatus,
            String comentario) {

        if (solicitacao == null) {
            throw new IllegalArgumentException("Solicitação não pode ser nula");
        }

        if (novoStatus == null) {
            throw new IllegalArgumentException("Novo status não pode ser nulo");
        }

        // Regra de negócio: só permite análise se status atual == EM_ANALISE
        if (solicitacao.getStatus() != StatusSolicitacao.EM_ANALISE) {
            throw new IllegalStateException(
                    String.format(
                            "Não é possível analisar uma solicitação com status '%s'. " +
                            "Apenas solicitações com status 'EM_ANALISE' podem ser analisadas.",
                            solicitacao.getStatus()
                    )
            );
        }

        // Regra de negócio: novo status deve ser APROVADO ou REPROVADO
        if (novoStatus != StatusSolicitacao.APROVADO && novoStatus != StatusSolicitacao.REPROVADO) {
            throw new IllegalArgumentException(
                    String.format(
                            "Status '%s' não é válido para análise. " +
                            "Apenas 'APROVADO' ou 'REPROVADO' são permitidos.",
                            novoStatus
                    )
            );
        }

        // Atualizar a solicitação com os dados da análise
        solicitacao.setStatus(novoStatus);
        solicitacao.setComentarioAnalise(comentario);
        solicitacao.setDataAnalise(LocalDateTime.now());
    }
}

