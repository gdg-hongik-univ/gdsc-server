package com.gdschongik.gdsc.domain.event.dto.request;

import com.gdschongik.gdsc.domain.common.vo.Period;
import com.gdschongik.gdsc.domain.event.domain.UsageStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public record EventUpdateBasicInfoRequest(
        @NotBlank String name,
        String venue,
        @NotNull LocalDateTime startAt,
        String description,
        @NotNull Period applicationPeriod,
        @NotNull UsageStatus regularRoleOnlyStatus,
        @Positive Integer mainEventMaxApplicantCount,
        @Positive Integer afterPartyMaxApplicantCount) {}
