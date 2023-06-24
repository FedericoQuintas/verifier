package com.challenge.verifier.placeOrder.persistence;

import com.challenge.verifier.matchOrder.domain.ReadQueueResult;
import com.challenge.verifier.placeOrder.domain.OrderPersistentModel;
import com.challenge.verifier.placeOrder.domain.Side;
import com.challenge.verifier.placeOrder.ports.OrdersPriorityQueue;
import com.challenge.verifier.placeOrder.stream.Result;
import org.apache.log4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisOrdersPriorityQueue implements OrdersPriorityQueue {

    private Logger logger = Logger.getLogger(RedisOrdersPriorityQueue.class);
    private RedisTemplate redisTemplate;
    private static final String BUY_KEY = "BuyRateByPriceAndTime|";
    private static final String SELL_KEY = "SellRateByPriceAndTime|";

    public RedisOrdersPriorityQueue(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Result add(OrderPersistentModel order) {
        Long orderId = order.getId();
        String key = order.isOnBuySide() ? BUY_KEY + orderId : SELL_KEY + orderId;
        Boolean result = redisTemplate.opsForZSet().add(key, order, score(order));
        if (result) {
            log(order, orderId, " was added to the queue with quantity: ");
            return Result.ok();
        } else {
            log(order, orderId, " could not be added to the queue with quantity: ");
            return Result.error();
        }
    }

    @Override
    public ReadQueueResult read(Side matchingSide) {
        return null;
    }

    private void log(OrderPersistentModel order, Long orderId, String s) {
        logger.info("Order " + orderId + s + order.getQuantity());
    }

    private Double score(OrderPersistentModel order) {
        return order.getPrice().doubleValue() - order.getTimestamp();
    }
}
