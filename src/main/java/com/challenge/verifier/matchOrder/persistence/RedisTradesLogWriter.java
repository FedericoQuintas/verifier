package com.challenge.verifier.matchOrder.persistence;

import com.challenge.verifier.matchOrder.ports.TradesLogWriter;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisTradesLogWriter implements TradesLogWriter {

    private String key;
    private RedisTemplate redisTemplate;
    private Logger logger = Logger.getLogger(RedisTradesLogWriter.class);

    public RedisTradesLogWriter(RedisTemplate redisTemplate, @Value("${REDIS_LOG_KEY}") String key) {
        this.redisTemplate = redisTemplate;
        this.key = key;
    }

    @Override
    public void append(String log) {
        Long result = redisTemplate.opsForList().rightPush(key, log);
        if (result == 1) {
            logger.info("Appends log successfully: " + log);
        } else {
            logger.info("Log append failed: " + log);
        }
    }

}
