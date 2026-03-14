package com.gdschongik.gdsc.domain.recruitment.domain.service;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.global.annotation.DomainService;
import com.gdschongik.gdsc.global.exception.CustomException;

@DomainService
public class RecruitmentValidator {

    public void validateRecruitmentCreate(boolean isRecruitmentSemesterOverlap, boolean isRecruitmentPeriodOverlap) {
        // 학년도와 학기가 같은 리쿠르팅이 이미 존재하는 경우
        if (isRecruitmentSemesterOverlap) {
            throw new CustomException(RECRUITMENT_SEMESTER_OVERLAP);
        }
        // 학기 기간이 겹치는 리쿠르팅이 이미 존재하는 경우
        if (isRecruitmentPeriodOverlap) {
            throw new CustomException(RECRUITMENT_PERIOD_OVERLAP);
        }
    }
}
