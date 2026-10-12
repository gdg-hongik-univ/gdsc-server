package com.gdschongik.gdsc.domain.statistic.domain;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.global.annotation.DomainService;
import com.gdschongik.gdsc.global.exception.CustomException;

@DomainService
public class CommunityStatisticValidator {

    public void validateSeminarCountUpdate(Member currentMember) {
        if (!currentMember.isAdmin()) {
            throw new CustomException(INVALID_ROLE);
        }
    }
}
