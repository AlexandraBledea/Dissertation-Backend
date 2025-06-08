package ubb.dissertation.benchmark.utils;

import org.springframework.stereotype.Component;
import ubb.dissertation.benchmark.dto.ExperimentDTO;
import ubb.dissertation.benchmark.entity.ExperimentEntity;

@Component
public class Mapper {

    public ExperimentDTO experimentEntityToExperimentDto(ExperimentEntity e) {
        return new ExperimentDTO(
                e.getId(),
                e.getBroker(),
                e.getNumberOfMessages(),
                e.getMessageSizeInKB(),
                e.getStartTime(),
                e.getEndTime(),
                e.getEnergy(),
                e.getStatus()
        );
    }
}
