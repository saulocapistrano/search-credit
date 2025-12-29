package br.com.searchcredit.domain.entity;

import br.com.searchcredit.domain.enums.StatusSolicitacao;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitacao_credito")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitacaoCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_credito", nullable = false, length = 50)
    private String numeroCredito;

    @Column(name = "numero_nfse", nullable = false, length = 50)
    private String numeroNfse;

    @Column(name = "data_constituicao", nullable = false)
    private LocalDate dataConstituicao;

    @Column(name = "valor_issqn", nullable = false)
    private BigDecimal valorIssqn;

    @Column(name = "tipo_credito", nullable = false, length = 50)
    private String tipoCredito;

    @Column(name = "simples_nacional", nullable = false)
    private boolean simplesNacional;

    @Column(name = "aliquota", nullable = false)
    private BigDecimal aliquota;

    @Column(name = "valor_faturado", nullable = false)
    private BigDecimal valorFaturado;

    @Column(name = "valor_deducao", nullable = false)
    private BigDecimal valorDeducao;

    @Column(name = "base_calculo", nullable = false)
    private BigDecimal baseCalculo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusSolicitacao status;

    @Column(name = "nome_solicitante", nullable = false, length = 255)
    private String nomeSolicitante;

    @Column(name = "comprovante_url", length = 500)
    private String comprovanteUrl;

    @Column(name = "data_solicitacao", nullable = false)
    private LocalDateTime dataSolicitacao;

    @Column(name = "comentario_analise", columnDefinition = "TEXT")
    private String comentarioAnalise;

    @Column(name = "data_analise")
    private LocalDateTime dataAnalise;
}

