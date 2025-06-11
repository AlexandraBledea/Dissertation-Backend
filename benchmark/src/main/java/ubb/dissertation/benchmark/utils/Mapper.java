package ubb.dissertation.benchmark.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import ubb.dissertation.benchmark.dto.Experiment;
import ubb.dissertation.benchmark.dto.ExperimentPaginationResponse;
import ubb.dissertation.benchmark.dto.Pagination;
import ubb.dissertation.benchmark.entity.ExperimentEntity;

import java.util.List;

import static java.util.stream.Collectors.toList;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Mapper {

    public static Experiment convertToExperiment(ExperimentEntity e) {
        return new Experiment(
                e.getId(),
                e.getBroker(),
                e.getNumberOfMessages(),
                e.getMessageSizeInKB(),
                e.getStartTime(),
                e.getEndTime(),
                e.getEnergy(),
                e.getAverageCpu(),
                e.getAverageMemory(),
                e.getThroughput(),
                e.getLatency(),
                e.getStatus()
        );
    }

    public static ExperimentPaginationResponse convertToExperimentPaginationResponse(Page<ExperimentEntity> experiments) {
        ExperimentPaginationResponse experimentPaginationResponse = new ExperimentPaginationResponse();
        Pagination pagination = new Pagination();
        pagination.setPageNumber(experiments.getPageable().getPageNumber());
        pagination.setPageSize(experiments.getPageable().getPageSize());
        pagination.setTotalNumberOfPages(experiments.getTotalPages());
        pagination.setTotalNumberOfItems(experiments.getNumberOfElements());
        List<Experiment> experimentList = experiments.stream()
                .map(Mapper::convertToExperiment)
                .collect(toList());
        experimentPaginationResponse.setExperiments(experimentList);
        experimentPaginationResponse.setPagination(pagination);
        return experimentPaginationResponse;
    }
}
