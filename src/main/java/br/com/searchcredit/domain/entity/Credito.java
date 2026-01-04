package br.com.searchcredit.domain.entity;

import br.com.searchcredit.domain.enums.StatusCredito;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "credito")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Credito {

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_credito", nullable = true, length = 50)
    private String numeroCredito;

    @Column(name = "numero_nfse", nullable = true, length = 50)
    private String numeroNfse;

    @Column(name = "data_constituicao", nullable = true)
    private LocalDate dataConstituicao;

    @Column(name = "valor_issqn", nullable = true)
    private BigDecimal valorIssqn;

    @Column(name = "tipo_credito", nullable = true, length = 50)
    private String tipoCredito;

    @Column(name = "simples_nacional", nullable = true)
    private Boolean simplesNacional;

    @Column(name = "aliquota", nullable = true)
    private BigDecimal aliquota;

    @Column(name = "valor_faturado", nullable = true)
    private BigDecimal valorFaturado;

    @Column(name = "valor_deducao", nullable = true)
    private BigDecimal valorDeducao;

    @Column(name = "base_calculo", nullable = true)
    private BigDecimal baseCalculo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = true, length = 20)
    private StatusCredito status;

    @Transient
    private String comprovanteUrl;

    @Column(name = "data_solicitacao", nullable = true)
    private LocalDateTime dataSolicitacao;

    @Column(name = "solicitado_por", nullable = true, length = 50)
    private String solicitadoPor;

    @Column(name = "aprovado_por", nullable = true, length = 50)
    private String aprovadoPor;

    @Column(name = "comentario_analise", nullable = true, columnDefinition = "TEXT")
    private String comentarioAnalise;

    @Column(name = "data_analise", nullable = true)
    private LocalDateTime dataAnalise;

    public boolean isSimplesNacional() {
        return Boolean.TRUE.equals(simplesNacional);
    }

}
