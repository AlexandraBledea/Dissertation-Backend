package ubb.dissertation.rabbitmq_consumer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RabbitController {

    @GetMapping("/status")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok("READY");
    }
}
