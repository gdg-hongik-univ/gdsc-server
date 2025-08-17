package com.gdschongik.gdsc.domain.event.dto.response;

import com.gdschongik.gdsc.domain.event.domain.AfterPartyApplicationStatus;
import com.gdschongik.gdsc.domain.event.domain.Participant;
import com.gdschongik.gdsc.domain.event.domain.ParticipantRole;

public record EventApplicantResponse(
        Participant participant,
        AfterPartyApplicationStatus afterPartyApplicationStatus,
        ParticipantRole participantRole) {}
