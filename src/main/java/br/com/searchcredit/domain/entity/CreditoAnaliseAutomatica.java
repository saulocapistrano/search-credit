package br.com.searchcredit.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "credito_analise_automatica",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_credito_analise_automatica_numero_credito", columnNames = "numero_credito")
        }
)
public class CreditoAnaliseAutomatica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_credito", nullable = false, length = 50)
    private String numeroCredito;

    @Column(name = "data_processamento", nullable = false)
    private LocalDateTime dataProcessamento;
}
