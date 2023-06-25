package com.challenge.verifier.reconcileOrderBook.ports;

import com.challenge.verifier.reconcileOrderBook.domain.TradeLogsResult;

public interface TradesLogReader {
    TradeLogsResult readAll();
}
