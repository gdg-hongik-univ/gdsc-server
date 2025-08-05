package com.gdschongik.gdsc.domain.event.domain;

public enum PaymentStatus {
    NONE,
    UNPAID,
    PAID,
    ;

    public static PaymentStatus getInitialPrePaymentStatus(Event event) {
        if (event.getPrePaymentStatus() == UsageStatus.DISABLED) {
            return NONE;
        }
        return UNPAID;
    }

    public static PaymentStatus getInitialPostPaymentStatus(Event event) {
        if (event.getPostPaymentStatus() == UsageStatus.DISABLED) {
            return NONE;
        }
        return UNPAID;
    }
}
