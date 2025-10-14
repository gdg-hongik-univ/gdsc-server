package com.gdschongik.gdsc.domain.event.dto.response;

import com.gdschongik.gdsc.domain.event.domain.AfterPartyApplicationStatus;
import com.gdschongik.gdsc.domain.event.domain.EventParticipation;
import com.gdschongik.gdsc.domain.event.domain.Participant;
import com.gdschongik.gdsc.domain.event.domain.ParticipantRole;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.querydsl.core.annotations.QueryProjection;
import jakarta.annotation.Nullable;

public record EventApplicantResponse(
        Long eventParticipationId,
        Participant participant,
        AfterPartyApplicationStatus afterPartyApplicationStatus,
        ParticipantRole participantRole,
        String discordUsername,
        String nickname) {

    @QueryProjection
    public EventApplicantResponse(EventParticipation eventParticipation, @Nullable Member member) {
        this(
                eventParticipation.getId(),
                eventParticipation.getParticipant(),
                eventParticipation.getAfterPartyApplicationStatus(),
                ParticipantRole.of(eventParticipation, member),
                member == null ? null : member.getDiscordUsername(),
                member == null ? null : member.getNickname());
    }
}
