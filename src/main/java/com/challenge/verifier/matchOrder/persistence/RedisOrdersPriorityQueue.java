package com.challenge.verifier.matchOrder.persistence;

import com.challenge.verifier.common.domain.Order;
import com.challenge.verifier.common.domain.OrderPersistentModel;
import com.challenge.verifier.common.domain.Side;
import com.challenge.verifier.matchOrder.domain.ReadQueueResult;
import com.challenge.verifier.matchOrder.domain.Result;
import com.challenge.verifier.matchOrder.domain.SnapshotResult;
import com.challenge.verifier.matchOrder.ports.OrdersPriorityQueue;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Service
public class RedisOrdersPriorityQueue implements OrdersPriorityQueue {

    private Logger logger = Logger.getLogger(RedisOrdersPriorityQueue.class);
    private RedisTemplate redisTemplate;
    private static final String BUY_KEY = "BuyQueue";
    private static final String SELL_KEY = "SellQueue";

    public RedisOrdersPriorityQueue(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Result add(OrderPersistentModel order) {
        String key = order.isOnBuySide() ? BUY_KEY : SELL_KEY;
        boolean result = redisTemplate.opsForZSet().add(key, order, score(order));
        if (result) {
            log(order, order.getId(), " was added to the queue " + key + " with quantity: ");
            return Result.ok();
        } else {
            log(order, order.getId(), " could not be added to the queue " + key + " with quantity: ");
            return Result.error();
        }
    }

    @Override
    public SnapshotResult snapshot() {
        try {
            List<OrderPersistentModel> buyQueue = fetchFromQueue(BUY_KEY);
            List<OrderPersistentModel> sellQueue = fetchFromQueue(SELL_KEY);
            return SnapshotResult.with(buyQueue, sellQueue);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return SnapshotResult.error();
        }
    }

    private List<OrderPersistentModel> fetchFromQueue(String key) {
        Set range = redisTemplate.opsForZSet().range(key, 0, -1);
        return range == null ? List.of() : range.stream().map(obj -> new ObjectMapper().convertValue(obj, OrderPersistentModel.class)).toList();
    }

    @Override
    public ReadQueueResult readFrom(Side matchingSide) {
        String key = Side.BUY.equals(matchingSide) ? BUY_KEY : SELL_KEY;
        try {
            ZSetOperations.TypedTuple typedTuple = fetchTuple(key, matchingSide);
            if (queueIsEmpty(key, typedTuple)) return ReadQueueResult.empty();
            OrderPersistentModel orderPersistentModel = new ObjectMapper().convertValue(typedTuple.getValue(), OrderPersistentModel.class);
            return ReadQueueResult.with(Order.buildFrom(orderPersistentModel));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ReadQueueResult.error();
        }
    }

    /*
        Only added in case it helps reviewers to run test suites.
     */
    @Override
    public void deleteAll() {
        redisTemplate.delete(BUY_KEY);
        redisTemplate.delete(SELL_KEY);
    }

    private ZSetOperations.TypedTuple fetchTuple(String key, Side matchingSide) {
        if (Side.BUY.equals(matchingSide)) {
            return redisTemplate.opsForZSet().popMax(key);
        } else
            return redisTemplate.opsForZSet().popMin(key);
    }

    private boolean queueIsEmpty(String key, ZSetOperations.TypedTuple typedTuple) {
        if (typedTuple == null) logger.info("queue " + key + " is empty");
        return typedTuple == null;
    }

    private void log(OrderPersistentModel order, Long orderId, String s) {
        logger.info("Order " + orderId + s + order.getQuantity());
    }

    // Redis allows only one field for the score: I implemented a trick to combine price and timestamp asc/desc to make popMax work correctly
    private Double score(OrderPersistentModel order) {
        if (order.isOnBuySide()) {
            String value = order.getPrice() + "." + (Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli() - order.getTimestamp());
            return Double.valueOf(value);
        } else {
            return Double.valueOf(order.getPrice() + "." + order.getTimestamp());
        }
    }
}
