package com.gdschongik.gdsc.domain.event.domain;

public enum PaymentStatus {
    NONE,
    UNPAID,
    PAID,
    ;

    /**
     * 선입금 결제 상태의 초기값을 반환합니다.
     * 선입금이 비활성화된 이벤트의 경우 초기값은 NONE이고, 활성화된 경우 초기값은 UNPAID입니다.
     */
    public static PaymentStatus getInitialPrePaymentStatus(Event event) {
        if (event.getPrePaymentStatus() == UsageStatus.DISABLED) {
            return NONE;
        }
        return UNPAID;
    }

    /**
     * 후정산 결제 상태의 초기값을 반환합니다.
     * 후정산이 비활성화된 이벤트의 경우 초기값은 NONE이고, 활성화된 경우 초기값은 UNPAID입니다.
     */
    public static PaymentStatus getInitialPostPaymentStatus(Event event) {
        if (event.getPostPaymentStatus() == UsageStatus.DISABLED) {
            return NONE;
        }
        return UNPAID;
    }
}
