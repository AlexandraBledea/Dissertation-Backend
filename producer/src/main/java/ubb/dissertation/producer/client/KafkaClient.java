package ubb.dissertation.producer.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ubb.dissertation.common.Message;

@Component
public class KafkaClient implements MessageBrokerClient{

    KafkaTemplate<String, Message> kafkaTemplate;
    private final String topic;

    public KafkaClient(KafkaTemplate<String, Message> kafkaTemplate, @Value("${kafka-topic}")String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void sendMessage(Message message) {
        kafkaTemplate.send(topic, String.valueOf(message.getMessageNumber()), message);
    }
}
