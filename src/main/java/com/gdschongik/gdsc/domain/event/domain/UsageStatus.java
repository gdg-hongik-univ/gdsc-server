package com.gdschongik.gdsc.domain.event.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 행사 폼의 항목별 사용 여부를 나타내는 Enum입니다.
 */
@Getter
@RequiredArgsConstructor
public enum UsageStatus {
    ENABLED("활성화"),
    DISABLED("비활성화");

    private final String value;

    public boolean isEnabled() {
        return this == ENABLED;
    }

    public boolean isDisabled() {
        return this == DISABLED;
    }
}
