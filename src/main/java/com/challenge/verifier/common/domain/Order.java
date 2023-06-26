package com.challenge.verifier.common.domain;

import java.time.Instant;

public record Order(Id id, Side side, Quantity quantity, Price price, Instant timestamp) {

    public static final String BUY_INITIAL = "B";
    public static final String COMMA = ",";
    public static final int EXPECTED_FIELDS_AMOUNT = 4;
    private static final String SELL_INITIAL = "S";

    public static Order buildFrom(OrderPersistentModel orderPersistentModel) {
        return new Order(Id.of(orderPersistentModel.getId()), Side.valueOf(orderPersistentModel.getSide().toUpperCase()), Quantity.of(orderPersistentModel.getQuantity()), Price.of(orderPersistentModel.getPrice()),
                Instant.ofEpochMilli(orderPersistentModel.getTimestamp()));
    }

    public static Order buildFrom(String nextLine, Instant timestamp) {
        String[] parts = nextLine.split(COMMA);
        validateFieldsAmount(parts);
        String orderId = parse(parts[0], "Order Id is required");
        String price = parse(parts[2], "Price is required");
        String quantity = parse(parts[3], "Quantity is required");
        Side side = parseSide(parts[1]);
        return new Order(Id.of(Long.valueOf(orderId)), side, Quantity.of(Integer.parseInt(quantity)), Price.of(Integer.parseInt(price)), timestamp);
    }

    private static String parse(String input, String message) {
        if (input == null || input.isEmpty()) throw exception(message);
        return input;
    }

    private static void validateFieldsAmount(String[] parts) {
        if (parts.length < EXPECTED_FIELDS_AMOUNT) throw exception("Incomplete input");
    }

    private static Side parseSide(String side) {
        if (side == null || side.isEmpty()) throw exception("Side is required");
        if (BUY_INITIAL.equalsIgnoreCase(side)) return Side.BUY;
        if (SELL_INITIAL.equalsIgnoreCase(side)) return Side.SELL;
        throw exception("Invalid order side");
    }

    private static RuntimeException exception(String message) {
        return new RuntimeException(message);
    }

    public OrderPersistentModel asPersistentModel() {
        return new OrderPersistentModel(id().value(), side().name(), quantity().value(), price().value(), timestamp().toEpochMilli());
    }

    public boolean isOnBuySide() {
        return Side.BUY.equals(side);
    }

    public Order reduceQuantity(Quantity quantityToReduce) {
        return new Order(id, side, quantity.minus(quantityToReduce), price, timestamp);
    }

    public boolean hasRemainingQuantity() {
        return quantity.isMoreThan(Quantity.of(0));
    }
}
