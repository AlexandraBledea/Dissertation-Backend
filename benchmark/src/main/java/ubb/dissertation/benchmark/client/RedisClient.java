package ubb.dissertation.benchmark.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import ubb.dissertation.benchmark.config.ClientConfiguration;

@FeignClient(name = "redis", url = "${client.redis.url}", configuration = ClientConfiguration.class)
public interface RedisClient {

    @GetMapping("/status")
    String getStatus();
}
