package com.gdschongik.gdsc.domain.email.domain.event;

public record PreviousEmailVerifiedEvent(Long currentMemberId, Long previousMemberId) {}
