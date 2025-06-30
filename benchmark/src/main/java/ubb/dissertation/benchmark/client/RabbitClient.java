package ubb.dissertation.benchmark.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "rabbit", url = "${client.rabbit.url}")
public interface RabbitClient {

    @GetMapping("/status")
    String getStatus();
}
