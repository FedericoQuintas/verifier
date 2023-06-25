package com.challenge.verifier.reconcileOrderBook.persistence;

import com.challenge.verifier.reconcileOrderBook.domain.TradeLogsResult;
import com.challenge.verifier.reconcileOrderBook.ports.TradesLogReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RedisTradesLogReader implements TradesLogReader {

    private RedisTemplate redisTemplate;

    private String key;

    public RedisTradesLogReader(RedisTemplate redisTemplate, @Value("${REDIS_LOG_KEY}") String key) {
        this.redisTemplate = redisTemplate;
        this.key = key;
    }

    @Override
    public TradeLogsResult readAll() {
        List<String> range = redisTemplate.opsForList().range(key, 0, -1);
        if (range == null) return TradeLogsResult.empty();
        return TradeLogsResult.with(range);
    }
}
