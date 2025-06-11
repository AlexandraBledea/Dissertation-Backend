package ubb.dissertation.benchmark.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "experiment")
public class ExperimentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column
    private String broker;

    @Column(name = "number_of_messages")
    private Integer numberOfMessages;

    @Column(name = "message_size_in_kb")
    private Integer messageSizeInKB;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column
    private Double energy;

    @Column(name = "average_cpu")
    private Double averageCpu;

    @Column(name = "average_memory")
    private Double averageMemory;

    @Column
    private Double throughput;

    @Column
    private Double latency;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "csv_content")
    private byte[] csvContent;
}
