package br.com.searchcredit.infrastructure.kafka.event;

import br.com.searchcredit.domain.enums.StatusCredito;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditoAnalisadoEvent {

    private Long creditoId;
    private String numeroCredito;
    private String numeroNfse;
    private StatusCredito status;
    private LocalDateTime dataAnalise;
    private String comentarioAnalise;
}

