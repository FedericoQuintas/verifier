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
        boolean matchingComplete = false;
        while (!matchingComplete) {
            ReadQueueResult readQueueResult = ordersPriorityQueue.read(matchingSide);
            if (readQueueResult.isEmpty()) {
                addToPriorityQueue(order);
                return;
            }
            Order matchingOrder = readQueueResult.order();
            if (pricesMatch(order, matchingOrder)) {
                Quantity matchingQuantity = calculateMatchingQuantity(order, matchingOrder);
                matchingOrder = matchingOrder.reduceQuantity(matchingQuantity);
                order = order.reduceQuantity(matchingQuantity);
                matchingComplete = updateOrders(order, matchingOrder);
            } else {
                returnOrdersToPriorityQueue(order, matchingOrder);
                return;
            }
        }
    }

    private static Quantity calculateMatchingQuantity(Order order, Order matchingOrder) {
        return Quantity.of(Math.min(order.quantity().value(), matchingOrder.quantity().value()));
    }

    private boolean updateOrders(Order order, Order matchingOrder) {
        boolean matchingComplete = false;
        if (order.hasRemainingQuantity()) {
            orderRepository.saveAndFlush(buildEvent(order, EventType.ORDER_PARTIALLY_FILLED).asPersistentModel());
        } else {
            orderRepository.saveAndFlush(buildEvent(order, EventType.ORDER_FILLED).asPersistentModel());
            matchingComplete = true;
        }
        if (matchingOrder.hasRemainingQuantity()) {
            orderRepository.saveAndFlush(buildEvent(matchingOrder, EventType.ORDER_PARTIALLY_FILLED).asPersistentModel());
            addToPriorityQueue(matchingOrder);
        } else {
            orderRepository.saveAndFlush(buildEvent(matchingOrder, EventType.ORDER_FILLED).asPersistentModel());
        }
        return matchingComplete;
    }

    private void returnOrdersToPriorityQueue(Order order, Order matchingOrder) {
        addToPriorityQueue(order);
        addToPriorityQueue(matchingOrder);
    }

    private void addToPriorityQueue(Order order) {
        ordersPriorityQueue.add(order.asPersistentModel());
    }

    private static boolean pricesMatch(Order order, Order matchingOrder) {
        if (order.isOnBuySide()) {
            return order.price().isEqualOrGreaterThan(matchingOrder.price());
        } else {
            return matchingOrder.price().isEqualOrGreaterThan(order.price());
        }
    }

    private Event buildEvent(Order order, EventType eventType) {
        return Event.with(order, eventType, timeProvider.now());
    }
}
