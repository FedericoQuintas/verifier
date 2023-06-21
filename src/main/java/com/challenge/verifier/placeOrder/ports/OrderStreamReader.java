package com.challenge.verifier.placeOrder.ports;

import java.io.IOException;

public interface OrderStreamReader {
    void read(String textFile) throws IOException;
}
