package com.gdschongik.gdsc.domain.event.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AfterPartyStatusesUpdateRequest(
        @NotNull @Positive Long eventId, @NotNull AfterPartyUpdateTarget afterPartyUpdateTarget) {}
