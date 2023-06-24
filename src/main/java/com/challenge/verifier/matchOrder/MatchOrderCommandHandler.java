package com.challenge.verifier.matchOrder;

import com.challenge.verifier.matchOrder.domain.ReadQueueResult;
import com.challenge.verifier.placeOrder.domain.*;
import com.challenge.verifier.placeOrder.ports.OrderRepository;
import com.challenge.verifier.placeOrder.ports.OrdersPriorityQueue;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class MatchOrderCommandHandler {

    private Logger logger = Logger.getLogger(MatchOrderCommandHandler.class);
    private OrdersPriorityQueue ordersPriorityQueue;
    private OrderRepository orderRepository;
    private TimeProvider timeProvider;

    public MatchOrderCommandHandler(OrdersPriorityQueue ordersPriorityQueue, OrderRepository orderRepository, TimeProvider timeProvider) {
        this.ordersPriorityQueue = ordersPriorityQueue;
        this.orderRepository = orderRepository;
        this.timeProvider = timeProvider;
    }

    public void match(Order order) {
        Side matchingSide = order.isOnBuySide() ? Side.SELL : Side.BUY;
        ReadQueueResult readQueueResult = ordersPriorityQueue.read(matchingSide);
        if (readQueueResult.isEmpty()) {
            ordersPriorityQueue.add(order.asPersistentModel());
            return;
        }
        Order matchingOrder = readQueueResult.order();
        Quantity minQuantity = Quantity.of(Math.min(order.quantity().value(), matchingOrder.quantity().value()));
        matchingOrder = matchingOrder.reduceQuantity(minQuantity);
        order = order.reduceQuantity(minQuantity);
        processOrder(order);
        processOrder(matchingOrder);
    }

    private void processOrder(Order order) {
        if (order.hasRemainingQuantity()) {
            orderRepository.saveAndFlush(buildEvent(order, EventType.ORDER_PARTIALLY_FILLED).asPersistentModel());
            ordersPriorityQueue.add(order.asPersistentModel());
        } else {
            orderRepository.saveAndFlush(buildEvent(order, EventType.ORDER_FILLED).asPersistentModel());
        }
    }

    private Event buildEvent(Order order, EventType eventType) {
        return Event.with(order, eventType, timeProvider.now());
    }
}
