package com.gdschongik.gdsc.domain.part.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PartType {
    BACKEND("백엔드"),
    FRONTEND("프론트엔드"),
    AI("인공지능");

    private final String value;
}
