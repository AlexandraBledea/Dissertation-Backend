package ubb.dissertation.redis_consumer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisController {

    @GetMapping("/status")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok("READY");
    }
}
