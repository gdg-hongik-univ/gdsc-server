package com.gdschongik.gdsc.domain.event.domain;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.common.vo.Period;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.global.annotation.DomainService;
import com.gdschongik.gdsc.global.exception.CustomException;
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

    public EventParticipation applyEventForUnregistered(
            Participant participant,
            AfterPartyApplicationStatus afterPartyApplicationStatus,
            Event event,
            LocalDateTime now) {

        validateEventApplicationPeriod(event, now);
        validateNotRegularRoleAllowed(event);
        validateAfterPartyApplicationStatus(event, afterPartyApplicationStatus);

        AfterPartyAttendanceStatus afterPartyAttendanceStatus = AfterPartyAttendanceStatus.getInitialStatus(event);
        PaymentStatus prePaymentStatus = PaymentStatus.getInitialPrePaymentStatus(event);
        PaymentStatus postPaymentStatus = PaymentStatus.getInitialPostPaymentStatus(event);

        return EventParticipation.createOnlineForUnregistered(
                participant,
                afterPartyApplicationStatus,
                afterPartyAttendanceStatus,
                prePaymentStatus,
                postPaymentStatus,
                event);
    }

    private void validateEventApplicationPeriod(Event event, LocalDateTime now) {
        Period applicationPeriod = event.getApplicationPeriod();
        if (!applicationPeriod.isWithin(now)) {
            throw new CustomException(EVENT_NOT_APPLIABLE_APPLICATION_PERIOD_INVALID);
        }
    }

    private void validateMemberWhenOnlyRegularRoleAllowed(Event event, Member member) {
        if (event.getRegularRoleOnlyStatus().isEnabled() && !member.isRegular()) {
            throw new CustomException(EVENT_NOT_APPLIABLE_NOT_REGULAR_ROLE);
        }
    }

    private void validateNotRegularRoleAllowed(Event event) {
        if (event.getRegularRoleOnlyStatus().isEnabled()) {
            throw new CustomException(EVENT_NOT_APPLIABLE_NOT_REGULAR_ROLE);
        }
    }

    private void validateAfterPartyApplicationStatus(
            Event event, AfterPartyApplicationStatus afterPartyApplicationStatus) {
        if (event.getAfterPartyStatus().isEnabled() && afterPartyApplicationStatus.isNone()) {
            throw new CustomException(EVENT_NOT_APPLIABLE_AFTER_PARTY_NONE);
        }

        if (!event.getAfterPartyStatus().isEnabled() && !afterPartyApplicationStatus.isNone()) {
            throw new CustomException(EVENT_NOT_APPLIABLE_AFTER_PARTY_NOT_NONE);
        }
    }

    // TODO: joinOnsiteForRegistered, joinOnsiteForUnregistered 메서드 구현
    // TODO: 작업 분량이 많기에 메서드 하나씩 구현할 것
}
