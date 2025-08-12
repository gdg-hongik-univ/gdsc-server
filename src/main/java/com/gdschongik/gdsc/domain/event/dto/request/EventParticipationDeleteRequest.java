package com.gdschongik.gdsc.domain.event.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record EventParticipationDeleteRequest(@NotEmpty List<Long> eventParticipationIds) {}
