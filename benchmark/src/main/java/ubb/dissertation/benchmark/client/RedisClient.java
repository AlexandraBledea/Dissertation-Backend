package ubb.dissertation.benchmark.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "redis", url = "${client.redis.url}")
public interface RedisClient {

    @GetMapping("/status")
    String getStatus();
}
