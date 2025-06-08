package ubb.dissertation.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Random;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private long timestamp;
    private int messageNumber;
    private int numberOfMessages;
    private int messageSizeInKB;
    private byte[] data;

    public Message(int messageNumber, int total, int sizeKB) {
        this.messageNumber = messageNumber;
        this.numberOfMessages = total;
        this.messageSizeInKB = sizeKB;
        this.data = new byte[sizeKB * 1024];
        new Random().nextBytes(this.data);
        this.timestamp = System.currentTimeMillis();
    }

}
