package com.gdschongik.gdsc.domain.event.dto.request;

import com.gdschongik.gdsc.domain.event.dto.dto.ParticipantAfterPartyStatusDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record AfterPartyApplicantStatusUpdateRequest(
        @NotNull @Positive Long eventId, List<@Valid ParticipantAfterPartyStatusDto> participantsAfterPartyStatus) {}
