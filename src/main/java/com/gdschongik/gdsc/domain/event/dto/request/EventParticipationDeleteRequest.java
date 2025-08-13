package com.gdschongik.gdsc.domain.event.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record EventParticipationDeleteRequest(@NotEmpty List<@Positive Long> eventParticipationIds) {}
