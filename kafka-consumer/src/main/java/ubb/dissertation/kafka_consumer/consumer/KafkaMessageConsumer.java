package ubb.dissertation.kafka_consumer.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ubb.dissertation.common.Message;
import ubb.dissertation.common.OshiLogger;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class KafkaMessageConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaMessageConsumer.class);

    private final OshiLogger oshiLogger;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final AtomicInteger receivedCount = new AtomicInteger(0);
    private final AtomicInteger expectedTotal = new AtomicInteger(-1);
    private final AtomicBoolean shutdownInitiated = new AtomicBoolean(false);

    public KafkaMessageConsumer(@Value("${benchmark.monitoring-file}") String monitoringFile) throws IOException {
        this.oshiLogger = new OshiLogger(monitoringFile);
        scheduler.scheduleAtFixedRate(oshiLogger::log, 0, 1, TimeUnit.SECONDS);
    }

    @KafkaListener(topics = "${kafka-topic}")
    public void consume(List<Message> messages, Acknowledgment ack) {
        if (messages.isEmpty()) return;

        long now = System.currentTimeMillis();

        for (Message message : messages) {
            long latency = now - message.getTimestamp();
            oshiLogger.recordMessage(latency);

            expectedTotal.compareAndSet(-1, message.getNumberOfMessages());

            log.info("Kafka: Received message {} of {}, latency: {} ms",
                    message.getMessageNumber(), message.getNumberOfMessages(), latency);
        }

        int currentCount = receivedCount.addAndGet(messages.size());
        ack.acknowledge();

        if (expectedTotal.get() > 0 && currentCount >= expectedTotal.get()
                && shutdownInitiated.compareAndSet(false, true)) {
            shutdown();
        }
    }

    private void shutdown() {
        Executors.newSingleThreadScheduledExecutor()
                .schedule(() -> {
                    scheduler.shutdown();
                    System.exit(0);
                }, 3, TimeUnit.SECONDS);
    }

}
