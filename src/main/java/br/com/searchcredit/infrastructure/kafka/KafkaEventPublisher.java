package br.com.searchcredit.infrastructure.kafka;

import br.com.searchcredit.domain.entity.Credito;
import br.com.searchcredit.infrastructure.kafka.event.ConsultaCreditoEvent;
import br.com.searchcredit.infrastructure.kafka.event.CreditoAnalisadoEvent;
import br.com.searchcredit.infrastructure.kafka.event.SolicitacaoCreditoAnalisadaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventPublisher {
    private static final Logger logger = LoggerFactory.getLogger(KafkaEventPublisher.class);
    private static final String TOPIC_CONSULTA = "consulta-creditos-topic";

    private final KafkaTemplate<String, ConsultaCreditoEvent> consultaKafkaTemplate;
    private final KafkaTemplate<String, SolicitacaoCreditoAnalisadaEvent> analiseKafkaTemplate;
    private final KafkaTemplate<String, CreditoAnalisadoEvent> creditoAnalisadoKafkaTemplate;

    public KafkaEventPublisher(
            KafkaTemplate<String, ConsultaCreditoEvent> consultaKafkaTemplate,
            KafkaTemplate<String, SolicitacaoCreditoAnalisadaEvent> analiseKafkaTemplate,
            KafkaTemplate<String, CreditoAnalisadoEvent> creditoAnalisadoKafkaTemplate) {
        this.consultaKafkaTemplate = consultaKafkaTemplate;
        this.analiseKafkaTemplate = analiseKafkaTemplate;
        this.creditoAnalisadoKafkaTemplate = creditoAnalisadoKafkaTemplate;
    }

    public void publishConsultaCredito(ConsultaCreditoEvent event) {
        consultaKafkaTemplate.send(TOPIC_CONSULTA, event);
        logger.info("ConsultaCreditoEvent publicado para o tópico '{}': {}", TOPIC_CONSULTA, event);
    }

    public void publishSolicitacaoAnalisada(SolicitacaoCreditoAnalisadaEvent event) {
        analiseKafkaTemplate.send(TOPIC_CONSULTA, event);
        logger.info("SolicitacaoCreditoAnalisadaEvent publicado para o tópico '{}': {}", TOPIC_CONSULTA, event);
    }

    /**
     * Publica evento de crédito analisado (novo evento de domínio).
     * 
     * @param credito O crédito analisado
     */
    public void publishCreditoAnalisado(Credito credito) {
        if (credito == null) {
            logger.warn("Tentativa de publicar evento CreditoAnalisadoEvent com crédito nulo");
            return;
        }

        CreditoAnalisadoEvent event = new CreditoAnalisadoEvent(
                credito.getId(),
                credito.getNumeroCredito(),
                credito.getNumeroNfse(),
                credito.getStatus(),
                credito.getDataAnalise(),
                credito.getComentarioAnalise()
        );

        creditoAnalisadoKafkaTemplate.send(TOPIC_CONSULTA, event);
        logger.info("CreditoAnalisadoEvent publicado para o tópico '{}': {}", TOPIC_CONSULTA, event);
    }
}
