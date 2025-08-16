package com.gdschongik.gdsc.domain.event.dto.dto;

import com.gdschongik.gdsc.domain.event.domain.*;

public record EventParticipationDto(
        Long eventParticipationId,
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
                ParticipantDto.from(eventParticipation.getParticipant()),
                eventParticipation.getMemberId(),
                eventParticipation.getMainEventApplicationStatus(),
                eventParticipation.getAfterPartyApplicationStatus(),
                eventParticipation.getAfterPartyAttendanceStatus(),
                eventParticipation.getPrePaymentStatus(),
                eventParticipation.getPostPaymentStatus());
    }
}
