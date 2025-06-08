package ubb.dissertation.benchmark.service;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ubb.dissertation.benchmark.dto.ExperimentDTO;
import ubb.dissertation.benchmark.entity.ExperimentEntity;
import ubb.dissertation.benchmark.entity.Status;
import ubb.dissertation.benchmark.repository.ExperimentRepository;
import ubb.dissertation.benchmark.utils.Mapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExperimentService {

    private final ExperimentRepository experimentRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final Mapper mapper;

    public ExperimentService(ExperimentRepository experimentRepository, SimpMessagingTemplate messagingTemplate, Mapper mapper) {
        this.experimentRepository = experimentRepository;
        this.messagingTemplate = messagingTemplate;
        this.mapper = mapper;
    }

    public ExperimentEntity createRunningExperiment(String broker, int messages, int sizeKB) {
        ExperimentEntity e = new ExperimentEntity();
        e.setBroker(broker);
        e.setNumberOfMessages(messages);
        e.setMessageSizeInKB(sizeKB);
        e.setStartTime(LocalDateTime.now());
        e.setStatus(Status.RUNNING);
        return experimentRepository.save(e);
    }

    public void finalizeExperiment(ExperimentEntity e, File logFile, String csvPath, int exitCode) {
        e.setEndTime(LocalDateTime.now());
        e.setStatus(exitCode == 0 ? Status.COMPLETED : Status.FAILED);
        e.setCsvContent(readSafe(csvPath));
        e.setEnergy(parseEnergy(logFile));
        ExperimentEntity saved = experimentRepository.save(e);
        sendNotification(saved);
    }

    public void failExperiment(ExperimentEntity e) {
        e.setEndTime(LocalDateTime.now());
        e.setStatus(Status.FAILED);
        ExperimentEntity saved = experimentRepository.save(e);
        sendNotification(saved);
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

    public List<ExperimentDTO> findAll() {
        return experimentRepository.findAll().stream()
                .map(mapper::experimentEntityToExperimentDto)
                .toList();
    }

    public ExperimentDTO findById(Long id) {
        return experimentRepository.findById(id)
                .map(mapper::experimentEntityToExperimentDto)
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
        ExperimentDTO dto = mapper.experimentEntityToExperimentDto(e);
        messagingTemplate.convertAndSend("/topic/experiment-update", dto);
    }
}