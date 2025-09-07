package com.gdschongik.gdsc.domain.event.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AfterPartyStatusUpdateAllRequest(
        @NotNull @Positive Long eventId, AfterPartyUpdateTarget afterPartyUpdateTarget) {}
