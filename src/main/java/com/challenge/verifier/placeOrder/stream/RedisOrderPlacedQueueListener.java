package com.challenge.verifier.placeOrder.stream;

import org.apache.log4j.Logger;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisOrderPlacedQueueListener implements MessageListener {

    private Logger logger = Logger.getLogger(RedisOrderPlacedQueueListener.class);
    private RedisTemplate redisTemplate;

    public RedisOrderPlacedQueueListener(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String deserialize = String.valueOf(redisTemplate.getValueSerializer().deserialize(message.getBody()));
        logger.info("Received " + deserialize);
    }
}
