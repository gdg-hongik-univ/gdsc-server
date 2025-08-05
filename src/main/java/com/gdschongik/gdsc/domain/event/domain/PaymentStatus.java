package com.gdschongik.gdsc.domain.event.domain;

public enum PaymentStatus {
    NONE,
    UNPAID,
    PAID,
    ;

    public static PaymentStatus getInitialPrePaymentStatus(Event event) {
        if (event.getPrePaymentStatus().isEnabled()) {
            return UNPAID;
        }
        return NONE;
    }

    public static PaymentStatus getInitialPostPaymentStatus(Event event) {
        if (event.getPostPaymentStatus().isEnabled()) {
            return UNPAID;
        }
        return NONE;
    }
}
