package ubb.dissertation.rabbitmq_consumer.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ubb.dissertation.common.Message;
import ubb.dissertation.common.OshiLogger;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RabbitMessageConsumer {

    private static final Logger log = LoggerFactory.getLogger(RabbitMessageConsumer.class);

    private final OshiLogger oshiLogger;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final AtomicInteger receivedCount = new AtomicInteger(0);
    private final AtomicInteger expectedTotal = new AtomicInteger(-1);
    private final AtomicBoolean shutdownInitiated = new AtomicBoolean(false);


    public RabbitMessageConsumer(@Value("${benchmark.monitoring-file}") String monitoringFile) throws IOException {
        this.oshiLogger = new OshiLogger(monitoringFile);
        scheduler.scheduleAtFixedRate(oshiLogger::log, 0, 1, TimeUnit.SECONDS);
    }

    @RabbitListener(queues = "${rabbit.queue-name}")
    public void receiveMessage(Message message) {
        long now = System.currentTimeMillis();
        long latency = now - message.getTimestamp();
        oshiLogger.recordMessage(latency);

        log.info("RabbitMQ: Received message {} of {}, latency: {} ms", message.getMessageNumber(), message.getNumberOfMessages(), latency);

        expectedTotal.compareAndSet(-1, message.getNumberOfMessages());

        int currentCount = receivedCount.incrementAndGet();

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
