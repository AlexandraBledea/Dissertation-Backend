package ubb.dissertation.producer.client;


import ubb.dissertation.common.Message;

public interface MessageBrokerClient {
    void sendMessage(Message msg);
}
