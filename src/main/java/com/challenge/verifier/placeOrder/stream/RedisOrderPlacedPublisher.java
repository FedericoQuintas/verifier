package com.challenge.verifier.placeOrder.stream;

import com.challenge.verifier.placeOrder.domain.Event;
import com.challenge.verifier.placeOrder.domain.EventPersistentModel;
import com.challenge.verifier.placeOrder.ports.OrderPlacedPublisher;
import org.apache.log4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisOrderPlacedPublisher implements OrderPlacedPublisher {

    private Logger logger = Logger.getLogger(RedisOrderPlacedPublisher.class);
    private RedisTemplate redisTemplate;
    private static final String BUY_KEY = "BuyRateByPriceAndTime|";
    private static final String SELL_KEY = "SellRateByPriceAndTime|";

    public RedisOrderPlacedPublisher(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public PublisherResult publish(Event event) {
        EventPersistentModel persistentModel = event.asPersistentModel();
        String key = event.isOnBuySide() ? BUY_KEY + persistentModel.getId() : SELL_KEY + persistentModel.getId();
        Boolean result = redisTemplate.opsForZSet().add(key, persistentModel, score(persistentModel));
        if (result) {
            logger.info("Event " + persistentModel.getId() + " was published");
            return PublisherResult.ok();
        } else {
            logger.error("Event " + persistentModel.getId() + " could not be published");
            return PublisherResult.error();
        }
    }

    private Double score(EventPersistentModel event) {
        return event.getPrice().doubleValue() - event.getEvent_time().toEpochMilli();
    }
}
