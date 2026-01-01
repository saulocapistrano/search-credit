package br.com.searchcredit.application.dto.solicitacao;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SolicitacaoCreditoRequestDto {

    private String numeroCredito;

    private String numeroNfse;

    private LocalDate dataConstituicao;

    private BigDecimal valorIssqn;

    private String tipoCredito;

    private Boolean simplesNacional;

    private BigDecimal aliquota;

    private BigDecimal valorFaturado;

    private BigDecimal valorDeducao;

    private BigDecimal baseCalculo;

    @NotBlank(message = "Nome do solicitante é obrigatório")
    private String nomeSolicitante;
}

