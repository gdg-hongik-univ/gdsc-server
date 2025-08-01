package com.gdschongik.gdsc.domain.event.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EventType {
    ONLINE("온라인 행사"),
    OFFLINE("오프라인 행사"),
    ;

    private final String value;
}
