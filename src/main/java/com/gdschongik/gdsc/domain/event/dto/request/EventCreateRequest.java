package com.gdschongik.gdsc.domain.event.dto.request;

import com.gdschongik.gdsc.domain.common.vo.Period;
import com.gdschongik.gdsc.domain.event.domain.UsageStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public record EventCreateRequest(
        @NotBlank String name,
        String venue,
        @NotNull LocalDateTime startAt,
        @NotBlank String applicationDescription,
        @NotNull Period applicationPeriod,
        @NotNull UsageStatus regularRoleOnlyStatus,
        @NotNull UsageStatus afterPartyStatus,
        @NotNull UsageStatus prePaymentStatus,
        @NotNull UsageStatus postPaymentStatus,
        @NotNull UsageStatus rsvpQuestionStatus,
        @Positive Integer mainEventMaxApplicantCount,
        @Positive Integer afterPartyMaxApplicantCount) {}
