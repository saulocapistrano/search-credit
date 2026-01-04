package br.com.searchcredit.infrastructure.kafka.event;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class SolicitacaoCreditoEvent {

    private String numeroCredito;
    private String numeroNfse;
    private String solicitadoPor;
    private LocalDateTime dataHora;

    public SolicitacaoCreditoEvent(String numeroCredito, String numeroNfse, String solicitadoPor, LocalDateTime dataHora) {
        this.numeroCredito = numeroCredito;
        this.numeroNfse = numeroNfse;
        this.solicitadoPor = solicitadoPor;
        this.dataHora = dataHora;
    }

    public SolicitacaoCreditoEvent(String numeroCredito, String numeroNfse, String solicitadoPor) {
        this.numeroCredito = numeroCredito;
        this.numeroNfse = numeroNfse;
        this.solicitadoPor = solicitadoPor;
        this.dataHora = LocalDateTime.now();
    }
}
