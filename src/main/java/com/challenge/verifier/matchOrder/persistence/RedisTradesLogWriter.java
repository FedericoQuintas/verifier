package com.challenge.verifier.matchOrder.persistence;

import com.challenge.verifier.matchOrder.ports.TradesLogWriter;
import org.apache.log4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisTradesLogWriter implements TradesLogWriter {

    public static final String KEY = "TradesLog";
    private RedisTemplate redisTemplate;
    private Logger logger = Logger.getLogger(RedisTradesLogWriter.class);

    public RedisTradesLogWriter(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void append(String log) {
        Long result = redisTemplate.opsForList().rightPush(KEY, log);
        if (result == 1) {
            logger.info("Appends log successfully: " + log);
        } else {
            logger.info("Log append failed: " + log);
        }
    }
}
