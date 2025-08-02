package com.gdschongik.gdsc.domain.event.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UsageStatus {
    ENABLED("활성화"),
    DISABLED("비활성화");

    private final String value;
}
