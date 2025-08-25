package com.gdschongik.gdsc.domain.event.dto.request;

import com.gdschongik.gdsc.domain.event.domain.Participant;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EventUnregisteredApplyRequest(@NotNull @Positive Long eventId, Participant participant) {}
