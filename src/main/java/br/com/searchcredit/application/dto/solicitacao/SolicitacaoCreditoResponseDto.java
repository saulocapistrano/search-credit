package br.com.searchcredit.application.dto.solicitacao;

import br.com.searchcredit.domain.enums.StatusSolicitacao;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class SolicitacaoCreditoResponseDto {

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
    private StatusSolicitacao status;
    private String nomeSolicitante;
    private String comprovanteUrl;
    private LocalDateTime dataSolicitacao;
}

