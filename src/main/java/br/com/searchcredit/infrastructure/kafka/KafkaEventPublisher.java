package br.com.searchcredit.infrastructure.kafka;

import br.com.searchcredit.infrastructure.kafka.event.ConsultaCreditoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventPublisher {
    private static final Logger logger = LoggerFactory.getLogger(KafkaEventPublisher.class);
    private static final String TOPIC = "consulta-creditos-topic";

    private final KafkaTemplate<String, ConsultaCreditoEvent> kafkaTemplate;

    public KafkaEventPublisher(KafkaTemplate<String, ConsultaCreditoEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishConsultaCredito(ConsultaCreditoEvent event) {
        kafkaTemplate.send(TOPIC, event);
        logger.info("ConsultaCreditoEvent publicado para o tópico '{}': {}", TOPIC, event);
    }
}
