package com.gdschongik.gdsc.domain.event.domain;

import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.global.annotation.DomainService;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.exception.ErrorCode;

@DomainService
public class EventParticipationDomainService {

    public EventParticipation applyEventForRegistered(
            Member member, AfterPartyApplicationStatus afterPartyApplicationStatus, Event event) {

        // TODO: event.isAppliable(now) 사용하여 지원 기간 내인지 검증

        validateMemberWhenOnlyRegularRoleAllowed(event, member);

        // 이벤트 뒤풀이 상태가 비활성 상태인 경우, 뒤풀이 신청 상태가 NONE인지 검증하고 참여 상태를 NONE으로 설정
        // 이벤트 뒤풀이 상태가 활성 상태인 경우, 뒤풀이 신청 상태가 NONE이 아닌지 검증하고 참여 상태를 NOT_ATTENDED로 설정

        // TODO: validateAfterPartyApplicationStatus(event, afterPartyApplicationStatus);
        AfterPartyAttendanceStatus afterPartyAttendanceStatus = determineAfterPartyAttendanceStatus(event);

        // 이벤트 선입금 맟 후정산 상태가 비활성 상태인 경우, 선입금 및 후정산 상태를 NONE으로 설정
        // 이벤트 선입금 및 후정산 상태가 활성 상태인 경우, 선입금 및 후정산 상태를 UNPAID로 설정

        PaymentStatus prePaymentStatus = determinePrePaymentStatus(event);
        PaymentStatus postPaymentStatus = determinePostPaymentStatus(event);

        return EventParticipation.createOnlineForRegistered(
                member,
                afterPartyApplicationStatus,
                afterPartyAttendanceStatus,
                prePaymentStatus,
                postPaymentStatus,
                event);
    }

    private void validateMemberWhenOnlyRegularRoleAllowed(Event event, Member member) {
        if (event.getRegularRoleOnlyStatus() == UsageStatus.ENABLED && !member.isRegular()) {
            throw new CustomException(ErrorCode.EVENT_NOT_APPLIABLE_NOT_REGULAR_ROLE);
        }
    }

    private void validateNotRegularRoleAllowed(Event event) {
        // createXForUnregistered 메서드에서 사용
        if (event.getRegularRoleOnlyStatus() == UsageStatus.ENABLED) {
            throw new CustomException(ErrorCode.EVENT_NOT_APPLIABLE_NOT_REGULAR_ROLE);
        }
    }

    private AfterPartyAttendanceStatus determineAfterPartyAttendanceStatus(Event event) {
        if (event.getAfterPartyStatus() == UsageStatus.DISABLED) {
            return AfterPartyAttendanceStatus.NONE;
        } else {
            return AfterPartyAttendanceStatus.NOT_ATTENDED;
        }
    }

    private PaymentStatus determinePrePaymentStatus(Event event) {
        if (event.getPrePaymentStatus() == UsageStatus.DISABLED) {
            return PaymentStatus.NONE;
        } else {
            return PaymentStatus.UNPAID;
        }
    }

    private PaymentStatus determinePostPaymentStatus(Event event) {
        if (event.getPostPaymentStatus() == UsageStatus.DISABLED) {
            return PaymentStatus.NONE;
        } else {
            return PaymentStatus.UNPAID;
        }
    }

    // TODO: applyEventForUnregistered, joinOnsiteForRegistered, joinOnsiteForUnregistered 메서드 구현
    // TODO: 작업 분량이 많기에 메서드 하나씩 구현할 것
    // TODO: 도메인 서비스에 대한 테스트 추가
}
