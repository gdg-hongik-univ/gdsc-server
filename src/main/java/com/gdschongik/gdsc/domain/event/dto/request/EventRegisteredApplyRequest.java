package com.gdschongik.gdsc.domain.event.dto.request;

import com.gdschongik.gdsc.domain.event.domain.AfterPartyApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EventRegisteredApplyRequest(
        @NotNull @Positive Long eventId, @NotNull AfterPartyApplicationStatus afterPartyApplicationStatus) {}
