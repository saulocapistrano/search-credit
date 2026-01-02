package br.com.searchcredit.application.dto.credito;

import br.com.searchcredit.domain.enums.StatusCredito;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO de requisição para análise de um crédito.
 * 
 * Permite aprovar ou reprovar um crédito que está em análise.
 * O status deve ser APROVADO ou REPROVADO.
 */
@Data
public class CreditoAnaliseRequestDto {

    @NotNull(message = "Status é obrigatório")
    private StatusCredito status;

    private String comentarioAnalise;
}

