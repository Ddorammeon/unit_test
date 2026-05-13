package com.do_an.paymentservice.entity;

public enum PaymentMethod {
    CASH("Tiền mặt"),
    BANK_TRANSFER("Chuyển khoản ngân hàng");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
