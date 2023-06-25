package com.challenge.verifier.reconcileOrderBook.persistence;

import com.challenge.verifier.reconcileOrderBook.ports.TradesLogReader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.Assert.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("redis-test")
public class RedisTradesLogReaderTest {

    @Autowired
    TradesLogReader tradesLogReader;

    @Value("${REDIS_LOG_KEY}")
    private String key;

    @Autowired
    RedisTemplate redisTemplate;

    @Test
    void readNext() {
        redisTemplate.opsForList().rightPush(key, "trade 10000, 99999, 98, 20");
        redisTemplate.opsForList().rightPush(key, "trade 10001, 99991, 198, 320");
        redisTemplate.opsForList().rightPush(key, "trade 10002, 99994, 298, 220");
        redisTemplate.opsForList().rightPush(key, "trade 10003, 99996, 398, 120");
        List<String> logs = tradesLogReader.readAll().logs;
        assertEquals("trade 10000, 99999, 98, 20", logs.get(0));
        assertEquals("trade 10001, 99991, 198, 320", logs.get(1));
        assertEquals("trade 10002, 99994, 298, 220", logs.get(2));
        assertEquals("trade 10003, 99996, 398, 120", logs.get(3));
    }
}
