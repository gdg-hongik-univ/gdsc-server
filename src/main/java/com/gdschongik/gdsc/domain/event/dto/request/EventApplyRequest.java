package com.gdschongik.gdsc.domain.event.dto.request;

import com.gdschongik.gdsc.domain.event.domain.AfterPartyApplicationStatus;
import com.gdschongik.gdsc.domain.event.domain.Participant;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EventApplyRequest(
        @NotNull @Positive Long eventId,
        @NotNull Participant participant,
        @NotNull AfterPartyApplicationStatus afterPartyApplicationStatus) {}
