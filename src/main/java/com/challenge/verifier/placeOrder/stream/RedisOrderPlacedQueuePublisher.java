package com.challenge.verifier.placeOrder.stream;

import com.challenge.verifier.placeOrder.domain.OrderPersistentModel;
import com.challenge.verifier.placeOrder.ports.OrderPlacedPublisher;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
@Profile({"!test"})
public class RedisOrderPlacedQueuePublisher implements OrderPlacedPublisher {

    private Logger logger = Logger.getLogger(RedisOrderPlacedQueuePublisher.class);
    private RedisTemplate redisTemplate;
    private ChannelTopic topic;

    public RedisOrderPlacedQueuePublisher(RedisTemplate redisTemplate, ChannelTopic topic) {
        this.redisTemplate = redisTemplate;
        this.topic = topic;
    }

    @Override
    public Result publish(OrderPersistentModel order) {
        try {
            logger.info("Will publish " + order.getId());
            redisTemplate.convertAndSend(topic.getTopic(), order);
            logger.info("Published " + order.getId());
            return Result.ok();
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            return Result.error();
        }
    }
}
