package com.challenge.verifier.placeOrder.stream;

import com.challenge.verifier.matchOrder.handler.MatchOrderCommandHandler;
import com.challenge.verifier.common.domain.Order;
import com.challenge.verifier.common.domain.OrderPersistentModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Profile({"default", "prod"})
public class RedisOrderPlacedQueueListener implements MessageListener {

    private Logger logger = Logger.getLogger(RedisOrderPlacedQueueListener.class);
    private MatchOrderCommandHandler matchOrderCommandHandler;


    public RedisOrderPlacedQueueListener(MatchOrderCommandHandler matchOrderCommandHandler, RedisMessageListenerContainer container, ChannelTopic topic) {
        this.matchOrderCommandHandler = matchOrderCommandHandler;
        setUpListener(container, topic);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            byte[] serialized = message.getBody();
            OrderPersistentModel orderPersistentModel = new ObjectMapper().readValue(serialized, OrderPersistentModel.class);
            logger.info("Received " + orderPersistentModel.getId());
            matchOrderCommandHandler.match(Order.buildFrom(orderPersistentModel));
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void setUpListener(RedisMessageListenerContainer container, ChannelTopic topic) {
        MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter();
        messageListenerAdapter.setDelegate(this);
        container.addMessageListener(messageListenerAdapter, topic);
    }
}
