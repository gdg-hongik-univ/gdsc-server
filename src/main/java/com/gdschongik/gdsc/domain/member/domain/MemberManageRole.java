package com.gdschongik.gdsc.domain.member.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberManageRole {
    ADMIN("ROLE_ADMIN"),
    NONE("ROLE_NONE");

    private final String value;

    public boolean isAdmin() {
        return this == ADMIN;
    }
}
