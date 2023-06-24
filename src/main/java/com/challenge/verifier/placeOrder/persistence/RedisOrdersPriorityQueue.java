package com.challenge.verifier.placeOrder.persistence;

import com.challenge.verifier.matchOrder.domain.ReadQueueResult;
import com.challenge.verifier.placeOrder.domain.Order;
import com.challenge.verifier.placeOrder.domain.OrderPersistentModel;
import com.challenge.verifier.placeOrder.domain.Side;
import com.challenge.verifier.placeOrder.ports.OrdersPriorityQueue;
import com.challenge.verifier.placeOrder.stream.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

@Service
public class RedisOrdersPriorityQueue implements OrdersPriorityQueue {

    private Logger logger = Logger.getLogger(RedisOrdersPriorityQueue.class);
    private RedisTemplate redisTemplate;
    private static final String BUY_KEY = "BuyRateByPriceAndTime";
    private static final String SELL_KEY = "SellRateByPriceAndTime";

    public RedisOrdersPriorityQueue(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Result add(OrderPersistentModel order) {
        String key = order.isOnBuySide() ? BUY_KEY : SELL_KEY;
        Boolean result = redisTemplate.opsForZSet().add(key, order, score(order));
        if (result) {
            log(order, order.getId(), " was added to the queue with quantity: ");
            return Result.ok();
        } else {
            log(order, order.getId(), " could not be added to the queue with quantity: ");
            return Result.error();
        }
    }

    @Override
    public ReadQueueResult readFrom(Side matchingSide) {
        String key = Side.BUY.equals(matchingSide) ? BUY_KEY : SELL_KEY;
        try {
            ZSetOperations.TypedTuple typedTuple = fetchTuple(key, matchingSide);
            if (!canRetrieveValuesFromQueue(key, typedTuple)) return ReadQueueResult.empty();
            OrderPersistentModel orderPersistentModel = new ObjectMapper().convertValue(typedTuple.getValue(), OrderPersistentModel.class);
            return ReadQueueResult.with(Order.buildFrom(orderPersistentModel));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ReadQueueResult.error();
        }
    }

    private ZSetOperations.TypedTuple fetchTuple(String key, Side matchingSide) {
        if (Side.BUY.equals(matchingSide))
            return redisTemplate.opsForZSet().popMax(key);
        else
            return redisTemplate.opsForZSet().popMin(key);
    }

    private boolean canRetrieveValuesFromQueue(String key, ZSetOperations.TypedTuple typedTuple) {
        return queueIsEmpty(key) || typedTuple == null;
    }

    private boolean queueIsEmpty(String key) {
        return redisTemplate.opsForZSet().size(key) == 0;
    }

    private void log(OrderPersistentModel order, Long orderId, String s) {
        logger.info("Order " + orderId + s + order.getQuantity());
    }

    private Double score(OrderPersistentModel order) {
        return order.getPrice().doubleValue() - order.getTimestamp();
    }
}
