package ubb.dissertation.producer.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    private final String exchangeName;

    private final String queueName;

    private final String routingKey;

    public RabbitConfig(
            @Value("${rabbit.exchange}") String exchangeName,
            @Value("${rabbit.queue-name}") String queueName,
            @Value("${rabbit.routing-key}") String routingKey) {
        this.exchangeName = exchangeName;
        this.queueName = queueName;
        this.routingKey = routingKey;
    }

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public Queue experimentsQueue() {
        return new Queue(queueName);
    }

    @Bean
    public Binding experimentsBinding() {
        return BindingBuilder.bind(experimentsQueue()).to(directExchange()).with(routingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}
