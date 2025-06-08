package ubb.dissertation.kafka_consumer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KafkaController {

    @GetMapping("/status")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok("READY");
    }
}