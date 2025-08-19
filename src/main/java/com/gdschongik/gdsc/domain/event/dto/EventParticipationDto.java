package com.gdschongik.gdsc.domain.event.dto;

import com.gdschongik.gdsc.domain.event.domain.*;

public record EventParticipationDto(
        Long eventParticipationId,
        Participant participant,
        Long memberId,
        MainEventApplicationStatus mainEventApplicationStatus,
        AfterPartyApplicationStatus afterPartyApplicationStatus,
        AfterPartyAttendanceStatus afterPartyAttendanceStatus,
        PaymentStatus prePaymentStatus,
        PaymentStatus postPaymentStatus) {}
