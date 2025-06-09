package ubb.dissertation.benchmark.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ubb.dissertation.benchmark.dto.ExperimentDTO;
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

    @GetMapping
    public ResponseEntity<List<ExperimentDTO>> getAllExperiments() {
        List<ExperimentDTO> experiments = experimentService.findAll();
        return ResponseEntity.ok(experiments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExperimentDTO> getExperimentById(@PathVariable Long id) {
        ExperimentDTO experimentDTO = experimentService.findById(id);
        return ResponseEntity.ok(experimentDTO);
    }

    @GetMapping("/csv/{id}")
    public ResponseEntity<byte[]> getCsvForExperiment(@PathVariable Long id) {
        return ResponseEntity.ok(experimentService.getCsvContent(id));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<ExperimentDTO>> filterExperiments(
            @RequestParam(required = false) String broker,
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) Integer sizeKb
    ) {
        List<ExperimentDTO> filtered = experimentService.filterExperiments(broker, count, sizeKb);
        return ResponseEntity.ok(filtered);
    }
}
