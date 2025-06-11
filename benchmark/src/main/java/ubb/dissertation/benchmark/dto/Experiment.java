package ubb.dissertation.benchmark.dto;

import ubb.dissertation.benchmark.entity.Status;

import java.time.LocalDateTime;

public record Experiment(Long id, String broker, Integer numberOfMessages, Integer messageSizeInKB, LocalDateTime startTime,
                         LocalDateTime endTime, Double energy, Double averageCpu, Double averageMemory,
                         Double throughput, Double latency, Status status){
}