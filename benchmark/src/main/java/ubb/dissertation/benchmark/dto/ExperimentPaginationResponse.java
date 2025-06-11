package ubb.dissertation.benchmark.dto;

import lombok.Data;

import java.util.List;

@Data
public class ExperimentPaginationResponse {
    private Pagination pagination;
    private List<Experiment> experiments;
}
