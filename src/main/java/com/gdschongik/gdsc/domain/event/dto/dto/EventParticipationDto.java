package com.gdschongik.gdsc.domain.event.dto.dto;

import com.gdschongik.gdsc.domain.event.domain.*;

public record EventParticipationDto(
        Long eventParticipationId,
        // TODO: DTO 대신 VO를 사용하도록 변경
        ParticipantDto participant,
        Long memberId,
        MainEventApplicationStatus mainEventApplicationStatus,
        AfterPartyApplicationStatus afterPartyApplicationStatus,
        AfterPartyAttendanceStatus afterPartyAttendanceStatus,
        PaymentStatus prePaymentStatus,
        PaymentStatus postPaymentStatus) {

    public static EventParticipationDto from(EventParticipation eventParticipation) {
        return new EventParticipationDto(
                eventParticipation.getId(),
                new ParticipantDto(
                        eventParticipation.getParticipant().getName(),
                        eventParticipation.getParticipant().getStudentId(),
                        eventParticipation.getParticipant().getPhone()),
                eventParticipation.getMemberId(),
                eventParticipation.getMainEventApplicationStatus(),
                eventParticipation.getAfterPartyApplicationStatus(),
                eventParticipation.getAfterPartyAttendanceStatus(),
                eventParticipation.getPrePaymentStatus(),
                eventParticipation.getPostPaymentStatus());
    }
}
