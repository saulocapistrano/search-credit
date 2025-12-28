package br.com.searchcredit.application.dto.solicitacao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SolicitacaoCreditoRequestDto {

    @NotBlank(message = "Número do crédito é obrigatório")
    private String numeroCredito;

    @NotBlank(message = "Número da NFS-e é obrigatório")
    private String numeroNfse;

    @NotNull(message = "Data de constituição é obrigatória")
    private LocalDate dataConstituicao;

    @NotNull(message = "Valor ISSQN é obrigatório")
    private BigDecimal valorIssqn;

    @NotBlank(message = "Tipo de crédito é obrigatório")
    private String tipoCredito;

    @NotNull(message = "Simples Nacional é obrigatório")
    private Boolean simplesNacional;

    @NotNull(message = "Alíquota é obrigatória")
    private BigDecimal aliquota;

    @NotNull(message = "Valor faturado é obrigatório")
    private BigDecimal valorFaturado;

    @NotNull(message = "Valor de dedução é obrigatório")
    private BigDecimal valorDeducao;

    @NotNull(message = "Base de cálculo é obrigatória")
    private BigDecimal baseCalculo;

    @NotBlank(message = "Nome do solicitante é obrigatório")
    private String nomeSolicitante;
}

