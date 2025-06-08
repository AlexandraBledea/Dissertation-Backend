package ubb.dissertation.benchmark.service;

import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class ConsumerService {

    public Process startConsumer(String broker, File logFile, String monitoringFile) throws IOException {
        String jarPath = getJarPath(broker);

        return new ProcessBuilder(
                "java",
                "-javaagent:joularjx.jar",
                "-jar", jarPath,
                "--benchmark.monitoring-file=" + monitoringFile
        ).redirectErrorStream(true).redirectOutput(logFile).start();
    }

    private String getJarPath(String type) {
        return switch (type.toLowerCase()) {
            case "kafka" -> "kafka-consumer.jar";
            case "rabbitmq" -> "rabbitmq-consumer.jar";
            case "redis" -> "redis-consumer.jar";
            default -> throw new IllegalArgumentException("Unsupported broker: " + type);
        };
    }


}