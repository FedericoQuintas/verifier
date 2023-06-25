package com.challenge.verifier.matchOrder.persistence;

import com.challenge.verifier.matchOrder.ports.TradesLogWriter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.Assert.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("redis-test")
public class RedisTradesLogWriterTest {

    @Autowired
    TradesLogWriter tradesLogWriter;

    @Value("${REDIS_LOG_KEY}")
    private String key;

    @Autowired
    RedisTemplate redisTemplate;

    @Test
    void appendsLogAndPopsAsFIFO() {
        tradesLogWriter.append("trade 10000, 99999, 98, 20");
        tradesLogWriter.append("trade 10001, 99991, 198, 320");
        tradesLogWriter.append("trade 10002, 99994, 298, 220");
        tradesLogWriter.append("trade 10003, 99996, 398, 120");
        assertEquals("trade 10000, 99999, 98, 20", redisTemplate.opsForList().leftPop(key));
        assertEquals("trade 10001, 99991, 198, 320", redisTemplate.opsForList().leftPop(key));
        assertEquals("trade 10002, 99994, 298, 220", redisTemplate.opsForList().leftPop(key));
        assertEquals("trade 10003, 99996, 398, 120", redisTemplate.opsForList().leftPop(key));
    }

}
