package com.gdschongik.gdsc.domain.event.dto.request;

import com.gdschongik.gdsc.domain.event.domain.UsageStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EventUpdateFormInfoRequest(
        @NotBlank String applicationDescription,
        @NotNull UsageStatus afterPartyStatus,
        @NotNull UsageStatus prePaymentStatus,
        @NotNull UsageStatus postPaymentStatus,
        @NotNull UsageStatus rsvpQuestionStatus,
        @NotNull UsageStatus noticeConfirmQuestionStatus) {}
