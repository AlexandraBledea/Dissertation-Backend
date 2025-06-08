package ubb.dissertation.producer.client;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ubb.dissertation.common.Message;

@Component
public class RabbitClient implements MessageBrokerClient {

    private final RabbitTemplate rabbitTemplate;
    private final String exchangeName;
    private final String routingKey;

    public RabbitClient(RabbitTemplate rabbitTemplate,
                        @Value("${rabbit.exchange}") String exchangeName,
                        @Value("${rabbit.routing-key}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
    }

    @Override
    public void sendMessage(Message msg) {
        rabbitTemplate.convertAndSend(exchangeName, routingKey, msg);
    }

}
