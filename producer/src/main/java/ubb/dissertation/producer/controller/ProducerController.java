package ubb.dissertation.producer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ubb.dissertation.producer.client.KafkaClient;
import ubb.dissertation.producer.client.MessageBrokerClient;
import ubb.dissertation.producer.client.RabbitClient;
import ubb.dissertation.producer.client.RedisClient;
import ubb.dissertation.producer.service.ProducerService;

@RestController
@RequestMapping("/api/evaluation")
public class ProducerController {

    private final ProducerService producerService;
    private final RabbitClient rabbitClient;
    private final KafkaClient kafkaClient;
    private final RedisClient redisClient;


    public ProducerController(final ProducerService producerService, final RabbitClient rabbitClient,
                              KafkaClient kafkaClient, RedisClient redisClient) {
        this.producerService = producerService;
        this.rabbitClient = rabbitClient;
        this.kafkaClient = kafkaClient;
        this.redisClient = redisClient;
    }

    @PostMapping("/{type}")
    public ResponseEntity<String> send(@PathVariable("type") String type,
                                       @RequestParam int count,
                                       @RequestParam int sizeKB) {

        MessageBrokerClient broker = switch (type.toLowerCase()) {
            case "rabbitmq" -> rabbitClient;
            case "kafka" -> kafkaClient;
            case "redis" -> redisClient;
            default -> throw new IllegalArgumentException("Unsupported broker: " + type);
        };

        producerService.sendBatch(count, sizeKB, broker);
        return ResponseEntity.accepted().body("Producer triggered asynchronously");
    }
}
