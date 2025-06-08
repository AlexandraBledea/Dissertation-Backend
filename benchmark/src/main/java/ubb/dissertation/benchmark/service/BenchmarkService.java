package ubb.dissertation.benchmark.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ubb.dissertation.benchmark.entity.ExperimentEntity;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class BenchmarkService {

    private final ConsumerService consumerService;
    private final ProducerService producerService;
    private final ExperimentService experimentService;
    private final AtomicBoolean experimentRunning = new AtomicBoolean(false);

    @Value("${benchmark.files.energy-file}")
    private String energyFile;

    @Value("${benchmark.files.monitoring-file}")
    private String monitoringFile;


    public BenchmarkService(ConsumerService consumerService,
                            ProducerService producerService,
                            ExperimentService experimentService) {
        this.consumerService = consumerService;
        this.producerService = producerService;
        this.experimentService = experimentService;
    }

    public boolean isExperimentRunning() {
        return experimentRunning.get();
    }

    @Async
    public void runConsumerTest(String broker, int numberOfMessages, int messageSizeKB) {
        if (!experimentRunning.compareAndSet(false, true)) {
            log.warn("Another experiment is already running.");
            return;
        }

        ExperimentEntity experiment = experimentService.createRunningExperiment(broker, numberOfMessages, messageSizeKB);
        File logFile = new File(energyFile);
        try {
            Process consumer = consumerService.startConsumer(broker, logFile, monitoringFile);
            producerService.waitForStartup(consumer, broker);
            producerService.triggerProducer(broker, numberOfMessages, messageSizeKB);

            int exitCode = consumer.waitFor();
            experimentService.finalizeExperiment(experiment, logFile, monitoringFile, exitCode);

        } catch (Exception e) {
            experimentService.failExperiment(experiment);
        } finally {
            experimentRunning.set(false);
            try {
                Files.deleteIfExists(Paths.get(energyFile));
                Files.deleteIfExists(Paths.get(monitoringFile));
            } catch (IOException e) {
                log.error("Files could not be deleted", e);
            }
        }
    }
}

