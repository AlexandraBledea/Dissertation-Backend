package ubb.dissertation.redis_consumer.consumer;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ubb.dissertation.common.Message;
import ubb.dissertation.common.OshiLogger;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RedisMessageConsumer {

    private static final Logger log = LoggerFactory.getLogger(RedisMessageConsumer.class);

    private final OshiLogger oshiLogger;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final AtomicInteger receivedCount = new AtomicInteger(0);
    private final AtomicInteger expectedTotal = new AtomicInteger(-1);
    private final AtomicBoolean shutdownInitiated = new AtomicBoolean(false);

    private volatile long lastMessageTime = System.currentTimeMillis();

    public RedisMessageConsumer(@Value("${benchmark.monitoring-file}") String monitoringFile) throws IOException {
        this.oshiLogger = new OshiLogger(monitoringFile);
        scheduler.scheduleAtFixedRate(oshiLogger::log, 0, 1, TimeUnit.SECONDS);
    }


    @PostConstruct
    public void init() {
        // Fallback shutdown after 10s of no messages if expectedTotal was set
        scheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            int expected = expectedTotal.get();
            int received = receivedCount.get();

            if (expected > 0 && received < expected
                    && now - lastMessageTime > 10000
                    && shutdownInitiated.compareAndSet(false, true)) {

                log.warn("Timeout: expected {}, received {}. Forcing shutdown.", expected, received);
                shutdown();
            }
        }, 10, 10, TimeUnit.SECONDS);
    }

    public void handleMessage(Message message) {
        long now = System.currentTimeMillis();
        long latency = now - message.getTimestamp();
        oshiLogger.recordMessage(latency);

        log.info("Redis: Received message {} of {}, latency: {} ms", message.getMessageNumber(), message.getNumberOfMessages(), latency);

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
