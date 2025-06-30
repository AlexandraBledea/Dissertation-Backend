package ubb.dissertation.benchmark.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "kafka", url = "${client.kafka.url}")
public interface KafkaClient {

    @GetMapping("/status")
    String getStatus();
}
