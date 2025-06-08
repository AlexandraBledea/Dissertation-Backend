package ubb.dissertation.benchmark.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ubb.dissertation.benchmark.config.ClientConfiguration;

@FeignClient(name = "producer", url = "${client.producer.url}", configuration = ClientConfiguration.class)
public interface ProducerClient {

    @PostMapping("/producer/api/evaluation/{type}")
    String evaluate(@PathVariable("type") String type,
                    @RequestParam int count,
                    @RequestParam int sizeKB);
}
