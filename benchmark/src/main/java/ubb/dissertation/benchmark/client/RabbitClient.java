package ubb.dissertation.benchmark.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import ubb.dissertation.benchmark.config.ClientConfiguration;

@FeignClient(name = "rabbit", url = "${client.rabbit.url}", configuration = ClientConfiguration.class)
public interface RabbitClient {

    @GetMapping("/status")
    String getStatus();
}
