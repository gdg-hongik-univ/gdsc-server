package com.gdschongik.gdsc.domain.member.domain.event;

import com.gdschongik.gdsc.domain.member.domain.MemberRole;

public record MemberDiscordChangedEvent(
        Long memberId, MemberRole memberRole, String previousDiscordId, String currentDiscordId) {}
