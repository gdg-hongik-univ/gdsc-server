package com.gdschongik.gdsc.domain.study.domain.event;

public record StudyApplyCanceledEvent(String studyDiscordRoleId, String memberDiscordId, Long studyId, Long memberId) {}
