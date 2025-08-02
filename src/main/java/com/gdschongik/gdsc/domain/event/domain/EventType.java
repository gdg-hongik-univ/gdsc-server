package com.gdschongik.gdsc.domain.event.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventType {
    ONLINE("온라인 행사"),
    OFFLINE("오프라인 행사"),
    ;

    private final String value;
}
