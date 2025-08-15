package com.gdschongik.gdsc.domain.event.dto;

import com.gdschongik.gdsc.domain.event.domain.AfterPartyApplicationStatus;
import com.gdschongik.gdsc.domain.event.domain.AfterPartyAttendanceStatus;
import com.gdschongik.gdsc.domain.event.domain.MainEventApplicationStatus;
import com.gdschongik.gdsc.domain.event.domain.PaymentStatus;

public record EventParticipationDto(
        Long eventParticipationId,
        ParticipantDto participant,
        Long memberId,
        MainEventApplicationStatus mainEventApplicationStatus,
        AfterPartyApplicationStatus afterPartyApplicationStatus,
        AfterPartyAttendanceStatus afterPartyAttendanceStatus,
        PaymentStatus prePaymentStatus,
        PaymentStatus postPaymentStatus) {}
