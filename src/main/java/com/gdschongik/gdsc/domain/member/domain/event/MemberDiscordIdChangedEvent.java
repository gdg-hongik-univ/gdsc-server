package com.gdschongik.gdsc.domain.member.domain.event;

public record MemberDiscordIdChangedEvent(Long memberId, String previousDiscordId, String currentDiscordId) {
}
