package br.com.searchcredit.infrastructure.kafka.event;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConsultaCreditoEvent {

    private String tipoConsulta;
    private String valorConsulta;
    private LocalDateTime dataHora;

    public ConsultaCreditoEvent(String tipoConsulta, String valorConsulta, LocalDateTime dataHora) {
        this.tipoConsulta = tipoConsulta;
        this.valorConsulta = valorConsulta;
        this.dataHora = dataHora;
    }

    public ConsultaCreditoEvent(String tipoConsulta, String valorConsulta) {
        this.tipoConsulta = tipoConsulta;
        this.valorConsulta = valorConsulta;
        this.dataHora = LocalDateTime.now();
    }
}
