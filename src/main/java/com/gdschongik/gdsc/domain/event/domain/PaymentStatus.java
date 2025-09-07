package com.gdschongik.gdsc.domain.event.domain;

import static com.gdschongik.gdsc.global.exception.ErrorCode.PAYMENT_STATUS_ALREADY_UPDATED;
import static com.gdschongik.gdsc.global.exception.ErrorCode.PAYMENT_STATUS_NOT_UPDATABLE_NONE;

import com.gdschongik.gdsc.global.exception.CustomException;

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

    public boolean isPaid() {
        return this == PAID;
    }

    public boolean isUnpaid() {
        return this == UNPAID;
    }

    public boolean isNone() {
        return this == NONE;
    }

    public PaymentStatus confirm() {
        if (isNone()) throw new CustomException(PAYMENT_STATUS_NOT_UPDATABLE_NONE);
        if (isPaid()) throw new CustomException(PAYMENT_STATUS_ALREADY_UPDATED);
        return PAID;
    }

    public PaymentStatus revoke() {
        if (isNone()) throw new CustomException(PAYMENT_STATUS_NOT_UPDATABLE_NONE);
        if (isUnpaid()) throw new CustomException(PAYMENT_STATUS_ALREADY_UPDATED);
        return UNPAID;
    }
}
