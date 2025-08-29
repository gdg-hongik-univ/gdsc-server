package com.gdschongik.gdsc.domain.event.dto.dto;

import com.gdschongik.gdsc.domain.event.domain.AfterPartyAttendanceStatus;
import com.gdschongik.gdsc.domain.event.domain.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AfterPartyStatusDto(
        @NotNull @Positive
        Long eventParticipationId,
        @NotNull
        PaymentStatus prePaymentStatus,
        @NotNull
        AfterPartyAttendanceStatus afterPartyAttendanceStatus,
        @NotNull
        PaymentStatus postPaymentStatus) {}
