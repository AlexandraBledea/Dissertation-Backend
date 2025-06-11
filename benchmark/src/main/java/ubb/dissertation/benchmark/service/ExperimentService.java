package ubb.dissertation.benchmark.service;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ubb.dissertation.benchmark.dto.Experiment;
import ubb.dissertation.benchmark.dto.ExperimentPaginationResponse;
import ubb.dissertation.benchmark.entity.ExperimentEntity;
import ubb.dissertation.benchmark.entity.Status;
import ubb.dissertation.benchmark.repository.ExperimentRepository;
import ubb.dissertation.benchmark.utils.Mapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.data.domain.ExampleMatcher.StringMatcher.CONTAINING;

@Service
public class ExperimentService {

    private final ExperimentRepository experimentRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ExperimentService(ExperimentRepository experimentRepository, SimpMessagingTemplate messagingTemplate) {
        this.experimentRepository = experimentRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public ExperimentEntity createRunningExperiment(String broker, int messages, int sizeKB) {
        ExperimentEntity e = new ExperimentEntity();
        e.setBroker(broker);
        e.setNumberOfMessages(messages);
        e.setMessageSizeInKB(sizeKB);
        e.setStartTime(LocalDateTime.now());
        e.setStatus(Status.RUNNING);
        ExperimentEntity saved = experimentRepository.save(e);
        sendNotification(saved);
        return saved;
    }

    public void finalizeExperiment(ExperimentEntity e, File logFile, String csvPath, int exitCode) {
        e.setEndTime(LocalDateTime.now());
        e.setStatus(exitCode == 0 ? Status.COMPLETED : Status.FAILED);
        e.setCsvContent(readSafe(csvPath));
        e.setEnergy(parseEnergy(logFile));
        parseAndSetCsvMetrics(Path.of(csvPath), e);
        ExperimentEntity saved = experimentRepository.save(e);
        sendNotification(saved);
    }

    public void failExperiment(ExperimentEntity e) {
        e.setEndTime(LocalDateTime.now());
        e.setStatus(Status.FAILED);
        ExperimentEntity saved = experimentRepository.save(e);
        sendNotification(saved);
    }

    public void parseAndSetCsvMetrics(Path csvPath, ExperimentEntity experiment) {
        try (BufferedReader reader = Files.newBufferedReader(csvPath)) {
            String line = reader.readLine(); // Skip header
            if (line == null) return;

            Instant start = null, end = null;
            double sumCpu = 0, sumMem = 0;
            int count = 0;
            double lastLatency = 0;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 5) continue;

                double throughput = Double.parseDouble(parts[4]);
                if (throughput == 0.0) continue;  // Skip rows with 0 throughput

                Instant timestamp = Instant.parse(parts[0]);
                double cpu = Double.parseDouble(parts[1]);
                double mem = Double.parseDouble(parts[2]);
                double latency = Double.parseDouble(parts[3]);

                if (start == null) start = timestamp;
                end = timestamp;

                sumCpu += cpu;
                sumMem += mem;
                lastLatency = latency;
                count++;
            }

            if (count == 0 || start == null || end == null) return;

            double avgCpu = sumCpu / count;
            double avgMem = sumMem / count;
            double durationSec = Math.max(Duration.between(start, end).toMillis() / 1000.0, 1);
            double throughput = experiment.getNumberOfMessages() / durationSec;

            experiment.setAverageCpu(avgCpu);
            experiment.setAverageMemory(avgMem);
            experiment.setLatency(lastLatency);
            experiment.setThroughput(throughput);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private byte[] readSafe(String path) {
        try {
            return Files.readAllBytes(Path.of(path));
        } catch (IOException e) {
            return new byte[0];
        }
    }

    private Double parseEnergy(File logFile) {
        String regex = "Program consumed ([\\d.]+) joules";

        try (var lines = Files.lines(logFile.toPath())) {
            return lines
                    .map(line -> {
                        var matcher = java.util.regex.Pattern.compile(regex).matcher(line);
                        if (matcher.find()) {
                            return matcher.group(1);
                        }
                        return null;
                    })
                    .filter(s -> s != null && !s.isBlank())
                    .mapToDouble(Double::parseDouble)
                    .findFirst()
                    .orElse(0.0);
        } catch (IOException e) {
            return null;
        }
    }

    public ExperimentPaginationResponse getAllExperiments(int pageNumber, int pageSize, String broker, Integer numberOfMessages, Integer messageSize) {

        var pageRequest = PageRequest.of(pageNumber, pageSize);
        var experiment = new ExperimentEntity();
        experiment.setBroker(broker);
        experiment.setNumberOfMessages(numberOfMessages);
        experiment.setMessageSizeInKB(messageSize);
        var example = Example.of(experiment, ExampleMatcher.matching().withIgnoreCase());
        var entities = experimentRepository.findAll(example, pageRequest);
        return Mapper.convertToExperimentPaginationResponse(entities);
    }

    public List<Experiment> findAll() {
        return experimentRepository.findAll().stream()
                .map(Mapper::convertToExperiment)
                .toList();
    }

    public Experiment findById(Long id) {
        return experimentRepository.findById(id)
                .map(Mapper::convertToExperiment)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Experiment not found"));
    }

    public boolean isExperimentRunning() {
        return experimentRepository.existsByStatus(Status.RUNNING);
    }

    public byte[] getCsvContent(Long id) {
        return experimentRepository.findById(id)
                .map(ExperimentEntity::getCsvContent)
                .orElse(new byte[0]);
    }

    private void sendNotification(ExperimentEntity e) {
        Experiment dto = Mapper.convertToExperiment(e);
        messagingTemplate.convertAndSend("/topic/experiment-update", dto);
    }

    public List<Experiment> filterExperiments(String broker, Integer numberOfMessages, Integer messageSizeInKB) {
        Specification<ExperimentEntity> spec = Specification.where(
                (root, query, cb) -> cb.equal(root.get("status"), Status.COMPLETED)
        );

        if (broker != null && !broker.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("broker")), broker.toLowerCase()));
        }

        if (numberOfMessages != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("numberOfMessages"), numberOfMessages));
        }

        if (messageSizeInKB != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("messageSizeInKB"), messageSizeInKB));
        }

        return experimentRepository.findAll(spec).stream()
                .map(Mapper::convertToExperiment)
                .toList();
    }
}