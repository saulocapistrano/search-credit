package br.com.searchcredit.infrastructure.kafka;

import br.com.searchcredit.infrastructure.kafka.event.ConsultaCreditoEvent;
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

    public KafkaEventPublisher(
            KafkaTemplate<String, ConsultaCreditoEvent> consultaKafkaTemplate,
            KafkaTemplate<String, SolicitacaoCreditoAnalisadaEvent> analiseKafkaTemplate) {
        this.consultaKafkaTemplate = consultaKafkaTemplate;
        this.analiseKafkaTemplate = analiseKafkaTemplate;
    }

    public void publishConsultaCredito(ConsultaCreditoEvent event) {
        consultaKafkaTemplate.send(TOPIC_CONSULTA, event);
        logger.info("ConsultaCreditoEvent publicado para o tópico '{}': {}", TOPIC_CONSULTA, event);
    }

    public void publishSolicitacaoAnalisada(SolicitacaoCreditoAnalisadaEvent event) {
        analiseKafkaTemplate.send(TOPIC_CONSULTA, event);
        logger.info("SolicitacaoCreditoAnalisadaEvent publicado para o tópico '{}': {}", TOPIC_CONSULTA, event);
    }
}
