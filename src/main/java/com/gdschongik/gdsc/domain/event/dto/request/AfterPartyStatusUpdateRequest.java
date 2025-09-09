package com.gdschongik.gdsc.domain.event.dto.request;

import jakarta.validation.constraints.NotNull;

public record AfterPartyStatusUpdateRequest(@NotNull AfterPartyUpdateTarget afterPartyUpdateTarget) {}
