package br.com.searchcredit.application.dto.credito;

import br.com.searchcredit.domain.enums.StatusCredito;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CreditoAdminResponseDto {

    private Long id;
    private String numeroCredito;
    private String numeroNfse;
    private LocalDate dataConstituicao;
    private BigDecimal valorIssqn;
    private String tipoCredito;
    private String simplesNacional;
    private BigDecimal aliquota;
    private BigDecimal valorFaturado;
    private BigDecimal valorDeducao;
    private BigDecimal baseCalculo;

    private StatusCredito status;
    private LocalDateTime dataSolicitacao;
    private String solicitadoPor;
    private String aprovadoPor;
    private String comentarioAnalise;
    private LocalDateTime dataAnalise;
}
