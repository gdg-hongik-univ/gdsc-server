package com.gdschongik.gdsc.domain.event.domain.service;

import static com.gdschongik.gdsc.domain.event.domain.AfterPartyApplicationStatus.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.common.vo.Period;
import com.gdschongik.gdsc.domain.event.domain.*;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.global.annotation.DomainService;
import com.gdschongik.gdsc.global.exception.CustomException;
import jakarta.annotation.Nullable;
import java.time.LocalDateTime;

@DomainService
public class EventParticipationDomainService {

    /**
     * 기본 정보 입력을 마친 회원이 온라인을 통해 이벤트에 참여 신청하는 메서드입니다.
     */
    @Deprecated(forRemoval = true)
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
     * 기본 정보 입력을 마치지 않은 회원이나 비회원이 온라인을 통해 이벤트에 참여 신청하는 메서드입니다.
     */
    @Deprecated(forRemoval = true)
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
     * 회원이 어드민 수동등록을 통해 참여하는 메서드입니다.
     * 주로 본행사 현장등록 상황에서 뒤풀이 신청을 위해 사용됩니다. (뒤풀이 신청상태 APPLIED)
     * 뒤풀이가 없는 행사인 경우에도 히스토리를 남기기 위해 사용됩니다. (뒤풀이 신청상태 NONE)
     */
    @Deprecated(forRemoval = true)
    public EventParticipation applyManualForRegistered(Member member, Event event) {
        validateMemberWhenOnlyRegularRoleAllowed(event, member);
        validateMemberBasicInfoSatisfied(member);

        // 뒤풀이가 존재하는 경우에만 항상 신청 처리
        AfterPartyApplicationStatus afterPartyApplicationStatus = event.afterPartyExists() ? APPLIED : NONE;

        AfterPartyAttendanceStatus afterPartyAttendanceStatus = AfterPartyAttendanceStatus.getInitialStatus(event);
        PaymentStatus prePaymentStatus = PaymentStatus.getInitialPrePaymentStatus(event);
        PaymentStatus postPaymentStatus = PaymentStatus.getInitialPostPaymentStatus(event);

        return EventParticipation.createManualForRegistered(
                member,
                afterPartyApplicationStatus,
                afterPartyAttendanceStatus,
                prePaymentStatus,
                postPaymentStatus,
                event);
    }

    @Deprecated(forRemoval = true)
    public EventParticipation applyManualForUnregistered(
            Participant participant, Event event, boolean infoStatusSatisfiedMemberExists) {
        validateNotRegularRoleAllowed(event);
        validateMemberInfoForUnregistered(infoStatusSatisfiedMemberExists);

        // 뒤풀이가 존재하는 경우에만 항상 신청 처리
        AfterPartyApplicationStatus afterPartyApplicationStatus = event.afterPartyExists() ? APPLIED : NONE;

        AfterPartyAttendanceStatus afterPartyAttendanceStatus = AfterPartyAttendanceStatus.getInitialStatus(event);
        PaymentStatus prePaymentStatus = PaymentStatus.getInitialPrePaymentStatus(event);
        PaymentStatus postPaymentStatus = PaymentStatus.getInitialPostPaymentStatus(event);

        return EventParticipation.createManualForUnregistered(
                participant,
                afterPartyApplicationStatus,
                afterPartyAttendanceStatus,
                prePaymentStatus,
                postPaymentStatus,
                event);
    }

    /**
     * 회원이 뒤풀이 현장등록을 통해 뒤풀이에 확정 참여하는 메서드입니다.
     */
    @Deprecated(forRemoval = true)
    public EventParticipation joinOnsiteForRegistered(Member member, Event event) {
        validateMemberWhenOnlyRegularRoleAllowed(event, member);

        PaymentStatus prePaymentStatus = PaymentStatus.getInitialPrePaymentStatus(event);
        PaymentStatus postPaymentStatus = PaymentStatus.getInitialPostPaymentStatus(event);

        return EventParticipation.createOnsiteForRegistered(member, prePaymentStatus, postPaymentStatus, event);
    }

    /**
     * 비회원이 뒤풀이 현장등록을 통해 뒤풀이에 확정 참여하는 메서드입니다.
     */
    @Deprecated(forRemoval = true)
    public EventParticipation joinOnsiteForUnregistered(
            Participant participant, Event event, boolean infoStatusSatisfiedMemberExists) {
        validateNotRegularRoleAllowed(event);
        validateMemberInfoForUnregistered(infoStatusSatisfiedMemberExists);

        PaymentStatus prePaymentStatus = PaymentStatus.getInitialPrePaymentStatus(event);
        PaymentStatus postPaymentStatus = PaymentStatus.getInitialPostPaymentStatus(event);

        return EventParticipation.createOnsiteForUnregistered(participant, prePaymentStatus, postPaymentStatus, event);
    }

    /**
     * 온라인을 통해 이벤트에 참여 신청하는 메서드입니다.
     * 정회원 전용 이벤트 여부는 본 신청방식에서만 검증하며, 그 외 방식에서는 검증하지 않습니다.
     */
    public EventParticipation applyOnline(
            Participant participant,
            @Nullable Member member,
            AfterPartyApplicationStatus afterPartyApplicationStatus,
            Event event,
            LocalDateTime now) {
        validateEventApplicationPeriod(event, now);
        validateMemberWhenOnlyRegularRoleAllowedIfExists(event, member); // applyOnline에서만 수행
        validateAfterPartyApplicationStatus(event, afterPartyApplicationStatus);

        AfterPartyAttendanceStatus afterPartyAttendanceStatus = AfterPartyAttendanceStatus.getInitialStatus(event);
        PaymentStatus prePaymentStatus = PaymentStatus.getInitialPrePaymentStatus(event);
        PaymentStatus postPaymentStatus = PaymentStatus.getInitialPostPaymentStatus(event);

        return EventParticipation.createOnline(
                participant,
                member,
                afterPartyApplicationStatus,
                afterPartyAttendanceStatus,
                prePaymentStatus,
                postPaymentStatus,
                event);
    }

    /**
     * 어드민 수동등록을 통해 참여하는 메서드입니다.
     * 주로 본행사 현장등록 상황에서 뒤풀이 신청을 위해 사용됩니다. (뒤풀이 신청상태 APPLIED)
     * 뒤풀이가 없는 행사인 경우에도 히스토리를 남기기 위해 사용됩니다. (뒤풀이 신청상태 NONE)
     */
    public EventParticipation applyManual(Participant participant, @Nullable Member member, Event event) {
        // 뒤풀이가 존재하는 경우에만 항상 신청 처리
        AfterPartyApplicationStatus afterPartyApplicationStatus = event.afterPartyExists() ? APPLIED : NONE;

        AfterPartyAttendanceStatus afterPartyAttendanceStatus = AfterPartyAttendanceStatus.getInitialStatus(event);
        PaymentStatus prePaymentStatus = PaymentStatus.getInitialPrePaymentStatus(event);
        PaymentStatus postPaymentStatus = PaymentStatus.getInitialPostPaymentStatus(event);

        return EventParticipation.createManual(
                participant,
                member,
                afterPartyApplicationStatus,
                afterPartyAttendanceStatus,
                prePaymentStatus,
                postPaymentStatus,
                event);
    }

    /**
     * 뒤풀이 현장등록을 통해 뒤풀이에 확정 참여하는 메서드입니다.
     */
    public EventParticipation joinOnsite(Participant participant, @Nullable Member member, Event event) {
        PaymentStatus prePaymentStatus = PaymentStatus.getInitialPrePaymentStatus(event);
        PaymentStatus postPaymentStatus = PaymentStatus.getInitialPostPaymentStatus(event);

        return EventParticipation.createOnsite(participant, member, prePaymentStatus, postPaymentStatus, event);
    }

    // 검증 로직

    /**
     * 이벤트 신청 기간을 검증하는 메서드입니다.
     * 온라인 신청에만 사용됩니다.
     */
    private void validateEventApplicationPeriod(Event event, LocalDateTime now) {
        Period applicationPeriod = event.getApplicationPeriod();
        if (!applicationPeriod.isWithin(now)) {
            throw new CustomException(EVENT_NOT_APPLICABLE_APPLICATION_PERIOD_INVALID);
        }
    }

    /**
     * 뒤풀이 신청 상태를 검증하는 메서드입니다.
     * 온라인 신청에서만 사용됩니다.
     */
    private void validateAfterPartyApplicationStatus(
            Event event, AfterPartyApplicationStatus afterPartyApplicationStatus) {
        if (event.afterPartyExists() && afterPartyApplicationStatus.isNone()) {
            throw new CustomException(EVENT_NOT_APPLICABLE_AFTER_PARTY_NONE);
        }

        if (!event.afterPartyExists() && !afterPartyApplicationStatus.isNone()) {
            throw new CustomException(EVENT_NOT_APPLICABLE_AFTER_PARTY_DISABLED);
        }
    }

    /**
     * 정회원만 허용되는 이벤트일 경우, 회원의 역할을 검증하는 메서드입니다.
     * 회원 신청시에만 사용됩니다.
     */
    @Deprecated(forRemoval = true)
    private void validateMemberWhenOnlyRegularRoleAllowed(Event event, Member member) {
        if (event.getRegularRoleOnlyStatus().isEnabled() && !member.isRegular()) {
            throw new CustomException(EVENT_NOT_APPLICABLE_NOT_REGULAR_ROLE);
        }
    }

    /**
     * 비 정회원도 신청 가능한 이벤트인지 검증하는 메서드입니다.
     * 비회원 신청시에만 사용됩니다.
     */
    @Deprecated(forRemoval = true)
    private void validateNotRegularRoleAllowed(Event event) {
        if (event.getRegularRoleOnlyStatus().isEnabled()) {
            throw new CustomException(EVENT_NOT_APPLICABLE_NOT_REGULAR_ROLE);
        }
    }

    /**
     * 회원의 기본 정보가 작성되었는지 검증하는 메서드입니다.
     * 회원 신청 시 기본 정보 작성이 완료되어야 합니다.
     * ForRegistered 메서드들에서 사용됩니다.
     */
    @Deprecated(forRemoval = true)
    private void validateMemberBasicInfoSatisfied(Member member) {
        if (!member.getAssociateRequirement().isInfoSatisfied()) {
            throw new CustomException(EVENT_NOT_APPLICABLE_MEMBER_INFO_NOT_SATISFIED);
        }
    }

    /**
     * 비회원 신청 시 해당 학번으로 가입된 회원의 기본 정보 작성 여부를 검증하는 메서드입니다.
     * 비회원 신청 시에는 해당 학번으로 가입된 회원이 존재하지 않거나,
     * 존재하더라도 기본 정보가 작성되지 않아야 합니다.
     * ForUnregistered 메서드들에서 사용됩니다.
     */
    @Deprecated(forRemoval = true)
    private void validateMemberInfoForUnregistered(boolean infoStatusSatisfiedMemberExists) {
        if (infoStatusSatisfiedMemberExists) {
            throw new CustomException(EVENT_NOT_APPLICABLE_MEMBER_INFO_SATISFIED);
        }
    }

    /**
     * 정회원만 허용되는 이벤트인 경우, 신청정보에 해당하는 멤버정보 존재 여부 및 정회원 여부를 검증합니다.
     */
    private void validateMemberWhenOnlyRegularRoleAllowedIfExists(Event event, @Nullable Member member) {
        if (!event.getRegularRoleOnlyStatus().isEnabled()) {
            return;
        }

        if (member == null || !member.isRegular()) {
            throw new CustomException(EVENT_NOT_APPLICABLE_NOT_REGULAR_ROLE);
        }
    }
}
