package br.com.searchcredit.application.dto.credito;

import br.com.searchcredit.domain.enums.StatusCredito;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditoResponseDto {

    private Long id;

    private String numeroCredito;

    private String numeroNfse;

    private LocalDate dataConstituicao;

    private BigDecimal valorIssqn;

    private String simplesNacional;

    private BigDecimal valorFaturado;

    private StatusCredito status;

    private LocalDateTime dataSolicitacao;

    private LocalDateTime dataAnalise;
}
