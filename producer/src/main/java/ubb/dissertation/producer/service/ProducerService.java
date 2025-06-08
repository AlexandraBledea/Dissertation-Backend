package ubb.dissertation.producer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ubb.dissertation.common.Message;
import ubb.dissertation.producer.client.MessageBrokerClient;


@Service
public class ProducerService {
    private static final Logger log = LoggerFactory.getLogger(ProducerService.class);

    public void sendBatch(int count, int sizeKB, MessageBrokerClient client) {
        long start = System.currentTimeMillis();

        for(int i = 0; i < count; i++) {
            Message message = new Message(i + 1, count, sizeKB);
            client.sendMessage(message);
        }

        long end = System.currentTimeMillis();
        log.info("Sent {} messages of {}KB in {} ms", count, sizeKB, end - start);
    }
}
