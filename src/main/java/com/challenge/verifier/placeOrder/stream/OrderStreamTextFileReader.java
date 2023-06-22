package com.challenge.verifier.placeOrder.stream;

import com.challenge.verifier.placeOrder.domain.Order;
import com.challenge.verifier.placeOrder.domain.TimeProvider;
import com.challenge.verifier.placeOrder.handler.PlaceOrderCommandHandler;
import com.challenge.verifier.placeOrder.ports.OrderStreamReader;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Service
public class OrderStreamTextFileReader implements OrderStreamReader {

    private PlaceOrderCommandHandler placeOrderCommandHandler;
    private TimeProvider timeProvider;
    private Logger logger = Logger.getLogger(OrderStreamTextFileReader.class);

    public OrderStreamTextFileReader(PlaceOrderCommandHandler placeOrderCommandHandler, TimeProvider timeProvider) {
        this.placeOrderCommandHandler = placeOrderCommandHandler;
        this.timeProvider = timeProvider;
    }

    @Override
    public void read(String fileName) throws IOException {
        try (Stream<String> lines = Files.lines(Path.of(fileName))) {
            lines.forEachOrdered(line -> {
                logger.info("Reads line " + line);
                placeOrderCommandHandler.place(Order.buildFrom(line, timeProvider.now()));
            });
        }

    }
}
