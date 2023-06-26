package com.challenge.verifier.common.domain;


public record Id(Long value) {
    public static Id of(Long value) {
        return new Id(value);
    }
}
