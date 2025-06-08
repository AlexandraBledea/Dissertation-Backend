package ubb.dissertation.benchmark.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ubb.dissertation.benchmark.client.KafkaClient;
import ubb.dissertation.benchmark.client.ProducerClient;
import ubb.dissertation.benchmark.client.RabbitClient;
import ubb.dissertation.benchmark.client.RedisClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProducerService {

    private final ProducerClient producerClient;
    private final KafkaClient kafkaClient;
    private final RabbitClient rabbitClient;
    private final RedisClient redisClient;

    private static final int STARTUP_TIMEOUT_SEC = 20;
    private static final int POLL_INTERVAL_MS = 500;

    public void waitForStartup(Process process, String broker) throws InterruptedException {
        int waited = 0;
        while (waited < STARTUP_TIMEOUT_SEC * 1000) {
            try {
                String status = switch (broker.toLowerCase()) {
                    case "kafka" -> kafkaClient.getStatus();
                    case "rabbitmq" -> rabbitClient.getStatus();
                    case "redis" -> redisClient.getStatus();
                    default -> throw new IllegalArgumentException("Unsupported broker: " + broker);
                };

                if ("READY".equalsIgnoreCase(status)) {
                    log.info("Consumer for '{}' is ready", broker);
                    return;
                }
            } catch (Exception e) {
                log.debug("Waiting for '{}' consumer to become ready...", broker);
            }

            Thread.sleep(POLL_INTERVAL_MS);
            waited += POLL_INTERVAL_MS;
        }
        process.destroy();
        throw new RuntimeException("Timeout waiting for " + broker + " consumer startup");
    }

    public void triggerProducer(String broker, int messages, int sizeKB) {
        producerClient.evaluate(broker, messages, sizeKB);
    }
}