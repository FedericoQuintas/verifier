package com.challenge.verifier.matchOrder.ports;

public interface TradesLogWriter {
    void append(String log);

    void deleteAll();
}
