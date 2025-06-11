package ubb.dissertation.benchmark.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ubb.dissertation.benchmark.service.BenchmarkService;

import static ubb.dissertation.benchmark.utils.Validator.validateBrokerType;


@RestController
@RequestMapping("/api/experiment")
public class BenchmarkController {

    private final BenchmarkService benchmarkService;

    public BenchmarkController(BenchmarkService benchmarkService) {
        this.benchmarkService = benchmarkService;
    }

    @PostMapping("/start/{type}")
    public ResponseEntity<String> startBenchmark(@PathVariable("type") String type,
                                                 @RequestParam int count,
                                                 @RequestParam int sizeKb) {
        if (!validateBrokerType(type))
            return ResponseEntity.badRequest().body("Invalid broker type");

        if (benchmarkService.isExperimentRunning()) {
            return ResponseEntity.status(409).body("An experiment is already running. Please wait.");
        }

        benchmarkService.runConsumerTest(type, count, sizeKb);
        return ResponseEntity.ok().body("Benchmark started for " + type);
    }
}
