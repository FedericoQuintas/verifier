package com.challenge.verifier.placeOrder.domain;

public class Result {

    private boolean succeeded;

    private Result(boolean succeeded) {
        this.succeeded = succeeded;
    }

    public static Result ok() {
        return new Result(true);
    }

    public static Result error() {
        return new Result(false);
    }

    public boolean succeeded() {
        return succeeded;
    }
}
