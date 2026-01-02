package br.com.searchcredit.application.dto.credito;

import br.com.searchcredit.domain.enums.StatusCredito;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de resposta para operações com Crédito.
 * 
 * Representa um crédito completo com todos os seus campos,
 * incluindo informações de workflow (solicitação e análise).
 */
@Data
@Builder
public class CreditoResponseDto {

    private Long id;
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
    private StatusCredito status;
    private String nomeSolicitante;
    private String comprovanteUrl;
    private LocalDateTime dataSolicitacao;
    private String comentarioAnalise;
    private LocalDateTime dataAnalise;
}

