package com.gdschongik.gdsc.domain.event.dto.response;

import com.gdschongik.gdsc.domain.event.domain.AfterPartyApplicationStatus;
import com.gdschongik.gdsc.domain.event.domain.AfterPartyAttendanceStatus;
import com.gdschongik.gdsc.domain.event.domain.MainEventApplicationStatus;
import com.gdschongik.gdsc.domain.event.domain.PaymentStatus;

public record AfterPartyParticipationResponse(
        Long eventParticipationId,
        String participantName,
        String participantStudentId,
        String participantPhone,
        Long memberId,
        MainEventApplicationStatus mainEventApplicationStatus,
        AfterPartyApplicationStatus afterPartyApplicationStatus,
        AfterPartyAttendanceStatus afterPartyAttendanceStatus,
        PaymentStatus prePaymentStatus,
        PaymentStatus postPaymentStatus) {}
