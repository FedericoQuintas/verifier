package com.challenge.verifier.placeOrder;

import com.challenge.verifier.placeOrder.domain.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;


public class OrderTest {

    @Test
    public void buildBuyOrderFromString(){
        Order order = Order.buildFrom("10000,B,98,25500");
        assertEquals(order.id(), Id.of("10000"));
        assertEquals(order.side(), Side.BUY);
        assertEquals(order.price(), Price.of(BigDecimal.valueOf(98)));
        assertEquals(order.quantity(), Quantity.of(25500));
    }

    @Test
    public void buildSellOrderFromString(){
        Order order = Order.buildFrom("10000,S,98,25500");
        assertEquals(order.side(), Side.SELL);
    }

    @Test
    public void throwsExceptionWhenSideIsInvalid(){
        try{
            Order.buildFrom("10000,H,98,25500");
            fail();
        } catch (RuntimeException ex){
            assertEquals("Invalid order side", ex.getMessage());
        }
    }

    @Test
    public void throwsExceptionWhenAtLeastOneFieldIsMissing(){
        try{
            Order.buildFrom("10000,H,25500");
            fail();
        } catch (RuntimeException ex){
            assertEquals("Incomplete input", ex.getMessage());
        }
    }

    @Test
    public void throwsExceptionWhenIdIsNull(){
        try{
            Order.buildFrom(",S,98,25500");
            fail();
        } catch (RuntimeException ex){
            assertEquals("Order Id is required", ex.getMessage());
        }
    }

    @Test
    public void throwsExceptionWhenSideIsNull(){
        try{
            Order.buildFrom("1000,,98,25500");
            fail();
        } catch (RuntimeException ex){
            assertEquals("Side is required", ex.getMessage());
        }
    }

    @Test
    public void throwsExceptionWhenPriceIsNull(){
        try{
            Order.buildFrom("1000,S,,25500");
            fail();
        } catch (RuntimeException ex){
            assertEquals("Price is required", ex.getMessage());
        }
    }

    @Test
    public void throwsExceptionWhenQuantityIsNull(){
        try{
            Order.buildFrom("1000,S,98,");
            fail();
        } catch (RuntimeException ex){
            assertEquals("Incomplete input", ex.getMessage());
        }
    }
}
