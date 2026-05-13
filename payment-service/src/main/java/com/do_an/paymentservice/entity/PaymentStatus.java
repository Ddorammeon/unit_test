package com.do_an.paymentservice.entity;

public enum PaymentStatus {

    PENDING("Đang chờ thanh toán"),
    SUCCESSFUL("Thanh toán thành công"),
    FAILED("Thanh toán thất bại"),
    CANCELLED("Thanh toán đã hủy"),
    TIMEOUT("Quá thời gian"),

    REFUNDED("Đã hoàn tiền");
    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}