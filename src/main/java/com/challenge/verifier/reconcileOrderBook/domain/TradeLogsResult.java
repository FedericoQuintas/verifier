package com.challenge.verifier.reconcileOrderBook.domain;

public class TradeLogsResult {

    public String log;

    private TradeLogsResult(String log) {
        this.log = log;
    }

    private TradeLogsResult() {
    }

    public static TradeLogsResult empty() {
        return new TradeLogsResult();
    }

    public static TradeLogsResult with(String log) {
        return new TradeLogsResult(log);
    }

}
