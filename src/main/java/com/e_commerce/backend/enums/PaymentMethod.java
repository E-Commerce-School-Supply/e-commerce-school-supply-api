package com.e_commerce.backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PaymentMethod {
    ABA, 
    KHQR, 
    CARD;
    @JsonCreator
    public static PaymentMethod fromString(String value) {
        return PaymentMethod.valueOf(value.toUpperCase());
    }
}