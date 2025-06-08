package ubb.dissertation.benchmark.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Validator {

    public static boolean validateBrokerType(String type) {
        return type.equals("kafka") || type.equals("rabbitmq") || type.equals("redis");
    }
}
