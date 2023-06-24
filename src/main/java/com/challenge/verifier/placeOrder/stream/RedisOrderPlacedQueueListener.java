package com.challenge.verifier.placeOrder.stream;

import com.challenge.verifier.matchOrder.MatchOrderCommandHandler;
import com.challenge.verifier.placeOrder.domain.Order;
import com.challenge.verifier.placeOrder.domain.OrderPersistentModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

import java.io.IOException;

public class RedisOrderPlacedQueueListener implements MessageListener {

    private Logger logger = Logger.getLogger(RedisOrderPlacedQueueListener.class);
    private MatchOrderCommandHandler matchOrderCommandHandler;

    public RedisOrderPlacedQueueListener(MatchOrderCommandHandler matchOrderCommandHandler) {
        this.matchOrderCommandHandler = matchOrderCommandHandler;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            byte[] serialized = message.getBody();
            logger.info("Received " + serialized);
            OrderPersistentModel orderPersistentModel = new ObjectMapper().readValue(serialized, OrderPersistentModel.class);
            matchOrderCommandHandler.match(Order.buildFrom(orderPersistentModel));
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
