package com.gdschongik.gdsc.domain.member.domain.event;

public record MemberDiscordIdUpdatedEvent(Long memberId, String previousDiscordId, String currentDiscordId) {}
