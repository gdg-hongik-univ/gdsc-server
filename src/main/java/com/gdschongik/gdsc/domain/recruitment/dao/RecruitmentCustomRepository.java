package com.gdschongik.gdsc.domain.recruitment.dao;

import com.gdschongik.gdsc.domain.recruitment.domain.Recruitment;
import java.time.LocalDateTime;
import java.util.Optional;

public interface RecruitmentCustomRepository {
    Optional<Recruitment> findCurrentRecruitment(LocalDateTime now);

    boolean existsPeriodOverlapping(LocalDateTime startDate, LocalDateTime endDate);
}
