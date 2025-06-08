package ubb.dissertation.benchmark.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import ubb.dissertation.benchmark.config.ClientConfiguration;

@FeignClient(name = "kafka", url = "${client.kafka.url}", configuration = ClientConfiguration.class)
public interface KafkaClient {

    @GetMapping("/status")
    String getStatus();
}
