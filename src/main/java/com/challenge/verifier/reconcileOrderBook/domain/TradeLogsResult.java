package com.challenge.verifier.reconcileOrderBook.domain;

import java.util.List;

public class TradeLogsResult {

    public List<String> logs;

    private TradeLogsResult(List<String> logs) {
        this.logs = logs;
    }

    private TradeLogsResult() {
    }

    public static TradeLogsResult empty() {
        return new TradeLogsResult();
    }

    public static TradeLogsResult with(List<String> logs) {
        return new TradeLogsResult(logs);
    }

}
