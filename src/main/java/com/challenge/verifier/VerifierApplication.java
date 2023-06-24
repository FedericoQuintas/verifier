package com.challenge.verifier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;

@SpringBootApplication(exclude = {RedisRepositoriesAutoConfiguration.class})
public class VerifierApplication {

    public static void main(String[] args) {
        SpringApplication.run(VerifierApplication.class, args);
    }
}
