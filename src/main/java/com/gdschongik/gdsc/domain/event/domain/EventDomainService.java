package com.gdschongik.gdsc.domain.event.domain;

import static com.gdschongik.gdsc.domain.event.domain.UsageStatus.*;
import static com.gdschongik.gdsc.domain.event.domain.UsageStatus.ENABLED;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.common.vo.Period;
import com.gdschongik.gdsc.global.annotation.DomainService;
import com.gdschongik.gdsc.global.exception.CustomException;

@DomainService
public class EventDomainService {

    public Event create(
            String name,
            String venue,
            String applicationDescription,
            Period applicationPeriod,
            UsageStatus regularRoleOnlyStatus,
            UsageStatus afterPartyStatus,
            UsageStatus prePaymentStatus,
            UsageStatus postPaymentStatus,
            UsageStatus rsvpQuestionStatus,
            Integer mainEventMaxApplicantCount,
            Integer afterPartyMaxApplicantCount) {
        validatePaymentDisabledWhenAfterPartyDisabled(afterPartyStatus, prePaymentStatus, postPaymentStatus);
        validatePrePaymentAndPostPayment(prePaymentStatus, postPaymentStatus);

        return Event.create(
                name,
                venue,
                applicationDescription,
                applicationPeriod,
                regularRoleOnlyStatus,
                afterPartyStatus,
                prePaymentStatus,
                postPaymentStatus,
                rsvpQuestionStatus,
                mainEventMaxApplicantCount,
                afterPartyMaxApplicantCount);
    }

    private void validatePaymentDisabledWhenAfterPartyDisabled(
            UsageStatus afterPartyStatus, UsageStatus prePaymentStatus, UsageStatus postPaymentStatus) {
        if (afterPartyStatus == DISABLED && (prePaymentStatus == ENABLED || postPaymentStatus == ENABLED)) {
            throw new CustomException(EVENT_NOT_CREATABLE_INVALID_PAYMENT_STATUS);
        }
    }

    private void validatePrePaymentAndPostPayment(UsageStatus prePaymentStatus, UsageStatus postPaymentStatus) {
        // 선입금이 활성화된 경우 후정산은 항상 비활성화되어야 함
    }
}
