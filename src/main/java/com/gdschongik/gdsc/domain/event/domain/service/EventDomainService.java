package com.gdschongik.gdsc.domain.event.domain.service;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.global.annotation.DomainService;
import com.gdschongik.gdsc.global.exception.CustomException;
import jakarta.annotation.Nullable;

@DomainService
public class EventDomainService {

    /**
     * 행사의 최대 신청자 수를 변경할 때, 현재 신청자 수보다 낮게 설정하는 경우 예외를 발생시킵니다.
     */
    public void validateWhenUpdateMaxApplicantCount(int currentApplicants, @Nullable Integer newMaxApplicantCount) {
        if (newMaxApplicantCount == null) { // 인원 제한을 없애는 경우 항상 허용
            return;
        }

        if (currentApplicants > newMaxApplicantCount) {
            throw new CustomException(EVENT_NOT_UPDATABLE_MAX_APPLICANT_COUNT_INVALID);
        }
    }
}
