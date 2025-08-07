package com.gdschongik.gdsc.domain.event.domain;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.common.vo.Period;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.global.annotation.DomainService;
import com.gdschongik.gdsc.global.exception.CustomException;
import java.time.LocalDateTime;

@DomainService
public class EventParticipationDomainService {

    /**
     * 회원이 온라인을 통해 참여 신청하는 메서드입니다.
     */
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

    /**
     * 비회원이 온라인을 통해 참여 신청하는 메서드입니다.
     */
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

    /**
     * 회원이 뒤풀이 현장등록을 통해 참여 신청하는 메서드입니다.
     */
    public EventParticipation joinOnsiteForRegistered(Member member, Event event) {
        validateMemberWhenOnlyRegularRoleAllowed(event, member);

        PaymentStatus prePaymentStatus = PaymentStatus.getInitialPrePaymentStatus(event);
        PaymentStatus postPaymentStatus = PaymentStatus.getInitialPostPaymentStatus(event);

        return EventParticipation.createOnsiteForRegistered(member, prePaymentStatus, postPaymentStatus, event);
    }

    /**
     * 비회원이 뒤풀이 현장등록을 통해 참여 신청하는 메서드입니다.
     */
    public EventParticipation joinOnsiteForUnregistered(Participant participant, Event event) {
        validateNotRegularRoleAllowed(event);

        PaymentStatus prePaymentStatus = PaymentStatus.getInitialPrePaymentStatus(event);
        PaymentStatus postPaymentStatus = PaymentStatus.getInitialPostPaymentStatus(event);

        return EventParticipation.createOnsiteForUnregistered(participant, prePaymentStatus, postPaymentStatus, event);
    }

    // 검증 로직

    /**
     * 이벤트 신청 기간을 검증하는 메서드입니다.
     * 온라인 신청에만 사용됩니다.
     */
    private void validateEventApplicationPeriod(Event event, LocalDateTime now) {
        Period applicationPeriod = event.getApplicationPeriod();
        if (!applicationPeriod.isWithin(now)) {
            throw new CustomException(EVENT_NOT_APPLIABLE_APPLICATION_PERIOD_INVALID);
        }
    }

    /**
     * 뒤풀이 신청 상태를 검증하는 메서드입니다.
     * 온라인 신청에서만 사용됩니다.
     */
    private void validateAfterPartyApplicationStatus(
            Event event, AfterPartyApplicationStatus afterPartyApplicationStatus) {
        if (event.getAfterPartyStatus().isEnabled() && afterPartyApplicationStatus.isNone()) {
            throw new CustomException(EVENT_NOT_APPLIABLE_AFTER_PARTY_NONE);
        }

        if (!event.getAfterPartyStatus().isEnabled() && !afterPartyApplicationStatus.isNone()) {
            throw new CustomException(EVENT_NOT_APPLIABLE_AFTER_PARTY_NOT_NONE);
        }
    }

    /**
     * 정회원만 허용되는 이벤트일 경우, 회원의 역할을 검증하는 메서드입니다.
     * 회원 신청시에만 사용됩니다.
     */
    private void validateMemberWhenOnlyRegularRoleAllowed(Event event, Member member) {
        if (event.getRegularRoleOnlyStatus().isEnabled() && !member.isRegular()) {
            throw new CustomException(EVENT_NOT_APPLIABLE_NOT_REGULAR_ROLE);
        }
    }

    /**
     * 비 정회원도 신청 가능한 이벤트인지 검증하는 메서드입니다.
     * 비회원 신청시에만 사용됩니다.
     */
    private void validateNotRegularRoleAllowed(Event event) {
        if (event.getRegularRoleOnlyStatus().isEnabled()) {
            throw new CustomException(EVENT_NOT_APPLIABLE_NOT_REGULAR_ROLE);
        }
    }
}
