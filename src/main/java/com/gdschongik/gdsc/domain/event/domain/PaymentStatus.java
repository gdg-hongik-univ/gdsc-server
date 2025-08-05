package com.gdschongik.gdsc.domain.event.domain;

/**
 * 선입금/후정산 정산 상태를 나타내는 Enum입니다.
 */
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
