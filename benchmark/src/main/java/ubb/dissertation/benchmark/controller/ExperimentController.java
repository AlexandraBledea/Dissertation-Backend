package ubb.dissertation.benchmark.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ubb.dissertation.benchmark.dto.Experiment;
import ubb.dissertation.benchmark.dto.ExperimentPaginationResponse;
import ubb.dissertation.benchmark.service.ExperimentService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/experiment")
public class ExperimentController {

    private final ExperimentService experimentService;

    public ExperimentController(ExperimentService experimentService) {
        this.experimentService = experimentService;
    }

    @GetMapping("/all")
    public ResponseEntity<ExperimentPaginationResponse> getExperimentsWithPagination(@RequestParam("pageNumber") int pageNumber,
                                                                                     @RequestParam("pageSize") int pageSize,
                                                                                     @RequestParam(required = false) String broker,
                                                                                     @RequestParam(required = false) Integer numberOfMessages,
                                                                                     @RequestParam(required = false) Integer messageSize) {
        ResponseEntity<ExperimentPaginationResponse> response;
        response = ResponseEntity.ok(experimentService.getAllExperiments(pageNumber, pageSize, broker, numberOfMessages, messageSize));
        return response;
    }

    @GetMapping
    public ResponseEntity<List<Experiment>> getAllExperiments() {
        List<Experiment> experiments = experimentService.findAll();
        return ResponseEntity.ok(experiments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Experiment> getExperimentById(@PathVariable Long id) {
        Experiment experiment = experimentService.findById(id);
        return ResponseEntity.ok(experiment);
    }

    @GetMapping("/csv/{id}")
    public ResponseEntity<byte[]> getCsvForExperiment(@PathVariable Long id) {
        return ResponseEntity.ok(experimentService.getCsvContent(id));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Experiment>> filterExperiments(
            @RequestParam(required = false) String broker,
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) Integer sizeKb
    ) {
        List<Experiment> filtered = experimentService.filterExperiments(broker, count, sizeKb);
        return ResponseEntity.ok(filtered);
    }
}
