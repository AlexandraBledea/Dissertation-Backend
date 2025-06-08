package ubb.dissertation.benchmark.dto;

import ubb.dissertation.benchmark.entity.Status;

import java.time.LocalDateTime;

public record ExperimentDTO(Long id, String broker, int numberOfMessages, int messageSizeInKB, LocalDateTime startTime,
                            LocalDateTime endTme, Double energy, Status status){
}