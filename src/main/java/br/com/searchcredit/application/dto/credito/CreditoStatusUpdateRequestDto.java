package br.com.searchcredit.application.dto.credito;

import br.com.searchcredit.domain.enums.StatusCredito;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO de requisição para atualização de status de um crédito.
 * 
 * Permite atualizar o status de um crédito, exceto para APROVADO e REPROVADO,
 * que devem ser definidos através do endpoint de análise.
 */
@Data
public class CreditoStatusUpdateRequestDto {

    @NotNull(message = "Status é obrigatório")
    private StatusCredito status;
}

