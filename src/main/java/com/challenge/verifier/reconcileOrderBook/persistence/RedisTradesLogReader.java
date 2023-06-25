package com.challenge.verifier.reconcileOrderBook.persistence;

import com.challenge.verifier.reconcileOrderBook.domain.TradeLogsResult;
import com.challenge.verifier.reconcileOrderBook.ports.TradesLogReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisTradesLogReader implements TradesLogReader {

    private RedisTemplate redisTemplate;

    private String key;

    public RedisTradesLogReader(RedisTemplate redisTemplate, @Value("${REDIS_LOG_KEY}") String key) {
        this.redisTemplate = redisTemplate;
        this.key = key;
    }

    @Override
    public TradeLogsResult readNext() {
        Object tradesLog = redisTemplate.opsForList().leftPop(key);
        if (tradesLog == null) return TradeLogsResult.empty();
        return TradeLogsResult.with(String.valueOf(tradesLog));
    }
}
