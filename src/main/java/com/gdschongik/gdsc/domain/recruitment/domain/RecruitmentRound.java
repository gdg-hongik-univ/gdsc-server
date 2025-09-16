package com.gdschongik.gdsc.domain.recruitment.domain;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.common.model.BaseEntity;
import com.gdschongik.gdsc.domain.common.vo.Period;
import com.gdschongik.gdsc.global.exception.CustomException;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecruitmentRound extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recruitment_round_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private RoundType roundType;

    private String name;

    @Embedded
    private Period period;

    @ManyToOne
    @JoinColumn(name = "recruitment_id")
    private Recruitment recruitment;

    @Builder(access = AccessLevel.PRIVATE)
    private RecruitmentRound(RoundType roundType, String name, final Period period, Recruitment recruitment) {
        this.name = name;
        this.roundType = roundType;
        this.period = period;
        this.recruitment = recruitment;
    }

    public static RecruitmentRound create(String name, RoundType roundType, Period period, Recruitment recruitment) {
        return RecruitmentRound.builder()
                .roundType(roundType)
                .name(name)
                .period(period)
                .recruitment(recruitment)
                .build();
    }

    public boolean isOpen() {
        return period.isOpen();
    }

    public void validatePeriodOverlap(LocalDateTime startDate, LocalDateTime endDate) {
        period.validatePeriodOverlap(startDate, endDate);
    }

    public void updateRecruitmentRound(String name, Period period, RoundType roundType) {
        this.name = name;
        this.period = period;
        this.roundType = roundType;
    }

    public void validatePeriodNotStarted(LocalDateTime now) {
        if (now.isAfter(period.getStartDate())) {
            throw new CustomException(RECRUITMENT_ROUND_START_DATE_ALREADY_PASSED);
        }
    }

    public boolean isFirstRound() {
        return roundType == RoundType.FIRST;
    }
}
