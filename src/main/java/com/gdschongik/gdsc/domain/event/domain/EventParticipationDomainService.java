package com.gdschongik.gdsc.domain.event.domain;

import com.gdschongik.gdsc.domain.common.vo.Period;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.global.annotation.DomainService;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.exception.ErrorCode;
import java.time.LocalDateTime;

@DomainService
public class EventParticipationDomainService {

    public EventParticipation applyEventForRegistered(
            Member member, AfterPartyApplicationStatus afterPartyApplicationStatus, Event event, LocalDateTime now) {

        validateEventApplicationPeriod(event, now);
        validateMemberWhenOnlyRegularRoleAllowed(event, member);
        validateAfterPartyApplicationStatus(event, afterPartyApplicationStatus);

        AfterPartyAttendanceStatus afterPartyAttendanceStatus = AfterPartyAttendanceStatus.getInitialStatus(event);
        PaymentStatus prePaymentStatus = PaymentStatus.getInitialPrePaymentStatus(event);
        PaymentStatus postPaymentStatus = PaymentStatus.getInitialPostPaymentStatus(event);

        return EventParticipation.createOnlineForRegistered(
                member,
                afterPartyApplicationStatus,
                afterPartyAttendanceStatus,
                prePaymentStatus,
                postPaymentStatus,
                event);
    }

    private void validateEventApplicationPeriod(Event event, LocalDateTime now) {
        Period applicationPeriod = event.getApplicationPeriod();
        if (!applicationPeriod.isWithin(now)) {
            throw new CustomException(ErrorCode.EVENT_NOT_APPLIABLE_APPLICATION_PERIOD_INVALID);
        }
    }

    private void validateMemberWhenOnlyRegularRoleAllowed(Event event, Member member) {
        if (event.getRegularRoleOnlyStatus() == UsageStatus.ENABLED && !member.isRegular()) {
            throw new CustomException(ErrorCode.EVENT_NOT_APPLIABLE_NOT_REGULAR_ROLE);
        }
    }

    private void validateAfterPartyApplicationStatus(
            Event event, AfterPartyApplicationStatus afterPartyApplicationStatus) {
        if (event.getAfterPartyStatus() == UsageStatus.ENABLED
                && afterPartyApplicationStatus == AfterPartyApplicationStatus.NONE) {
            throw new CustomException(ErrorCode.EVENT_NOT_APPLIABLE_AFTER_PARTY_NONE);
        }

        if (event.getAfterPartyStatus() == UsageStatus.DISABLED
                && afterPartyApplicationStatus != AfterPartyApplicationStatus.NONE) {
            throw new CustomException(ErrorCode.EVENT_NOT_APPLIABLE_AFTER_PARTY_NOT_NONE);
        }
    }

    private void validateNotRegularRoleAllowed(Event event) {
        // createXForUnregistered 메서드에서 사용
        if (event.getRegularRoleOnlyStatus() == UsageStatus.ENABLED) {
            throw new CustomException(ErrorCode.EVENT_NOT_APPLIABLE_NOT_REGULAR_ROLE);
        }
    }

    // TODO: applyEventForUnregistered, joinOnsiteForRegistered, joinOnsiteForUnregistered 메서드 구현
    // TODO: 작업 분량이 많기에 메서드 하나씩 구현할 것
}
