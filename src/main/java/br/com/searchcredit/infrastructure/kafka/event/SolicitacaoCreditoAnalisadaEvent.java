package br.com.searchcredit.infrastructure.kafka.event;

import br.com.searchcredit.domain.enums.StatusSolicitacao;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Evento Kafka publicado quando uma solicitação de crédito é analisada.
 * 
 * Publicado no tópico: "consulta-creditos-topic"
 * Serialização: JSON
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitacaoCreditoAnalisadaEvent {

    private Long solicitacaoId;
    private String numeroCredito;
    private StatusSolicitacao statusFinal;
    private LocalDateTime dataAnalise;
    private String comentario;
}

