package com.gdschongik.gdsc.domain.event.dto.dto;

import com.gdschongik.gdsc.domain.event.domain.AfterPartyAttendanceStatus;
import com.gdschongik.gdsc.domain.event.domain.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ParticipantAfterPartyStatusDto(
        @NotNull @Positive Long participantId,
        @NotNull PaymentStatus prePaymentStatus,
        @NotNull AfterPartyAttendanceStatus afterPartyAttendanceStatus,
        @NotNull PaymentStatus postPaymentStatus) {}
