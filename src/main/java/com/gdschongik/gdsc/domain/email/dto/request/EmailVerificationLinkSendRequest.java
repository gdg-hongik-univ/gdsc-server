package com.gdschongik.gdsc.domain.email.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EmailVerificationLinkSendRequest(@NotNull @Positive Long previousMemberId) {}
