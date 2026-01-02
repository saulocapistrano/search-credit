package br.com.searchcredit.application.dto.solicitacao;

import br.com.searchcredit.domain.enums.StatusCredito;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AtualizarStatusRequestDto {

    @NotNull(message = "Status é obrigatório")
    private StatusCredito status;
}

