package br.com.searchcredit.domain.service;

import br.com.searchcredit.domain.entity.Credito;
import br.com.searchcredit.domain.enums.StatusCredito;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Serviço de domínio responsável pelas regras de negócio relacionadas à análise de créditos.
 * 
 * Este serviço contém APENAS regras de domínio, sem dependências de infraestrutura.
 */
@Component
public class AnaliseSolicitacaoCreditoService {

    /**
     * Analisa um crédito, aplicando as regras de negócio.
     * 
     * @param credito O crédito a ser analisado
     * @param novoStatus O novo status a ser aplicado (APROVADO ou REPROVADO)
     * @param comentario O comentário da análise
     * @throws IllegalStateException Se o crédito não estiver em EM_ANALISE
     */
    public void analisarCredito(
            Credito credito,
            StatusCredito novoStatus,
            String comentario) {

        if (credito == null) {
            throw new IllegalArgumentException("Crédito não pode ser nulo");
        }

        if (novoStatus == null) {
            throw new IllegalArgumentException("Novo status não pode ser nulo");
        }

        // Regra de negócio: só permite análise se status atual == EM_ANALISE
        if (credito.getStatus() != StatusCredito.EM_ANALISE) {
            throw new IllegalStateException(
                    String.format(
                            "Não é possível analisar um crédito com status '%s'. " +
                            "Apenas créditos com status 'EM_ANALISE' podem ser analisados.",
                            credito.getStatus()
                    )
            );
        }

        // Regra de negócio: novo status deve ser APROVADO ou REPROVADO
        if (novoStatus != StatusCredito.APROVADO && novoStatus != StatusCredito.REPROVADO) {
            throw new IllegalArgumentException(
                    String.format(
                            "Status '%s' não é válido para análise. " +
                            "Apenas 'APROVADO' ou 'REPROVADO' são permitidos.",
                            novoStatus
                    )
            );
        }

        // Atualizar o crédito com os dados da análise
        credito.setStatus(novoStatus);
        credito.setComentarioAnalise(comentario);
        credito.setDataAnalise(LocalDateTime.now());
    }
}

