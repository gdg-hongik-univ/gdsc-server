package com.gdschongik.gdsc.domain.statistic.domain;

import com.gdschongik.gdsc.domain.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityStatistic extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "community_statistic_id")
    private Long id;

    private Integer totalMemberCount;

    private Integer totalProjectCount;

    private Integer totalSeminarCount;

    @Builder(access = AccessLevel.PRIVATE)
    private CommunityStatistic(Integer totalMemberCount, Integer totalProjectCount, Integer totalSeminarCount) {
        this.totalMemberCount = totalMemberCount;
        this.totalProjectCount = totalProjectCount;
        this.totalSeminarCount = totalSeminarCount;
    }

    /**
     * 최초 생성 시 및 테스트 작성 시에만 사용합니다.
     */
    public static CommunityStatistic create(
            Integer totalMemberCount, Integer totalProjectCount, Integer totalSeminarCount) {
        return CommunityStatistic.builder()
                .totalMemberCount(totalMemberCount)
                .totalProjectCount(totalProjectCount)
                .totalSeminarCount(totalSeminarCount)
                .build();
    }

    /**
     * 누적 회원 수를 수정합니다.
     */
    public void updateTotalMemberCount(int totalMemberCount) {
        this.totalMemberCount = totalMemberCount;
    }

    /**
     * 누적 프로젝트 수를 수정합니다.
     */
    public void updateTotalProjectCount(Integer totalProjectCount) {
        this.totalProjectCount = totalProjectCount;
    }

    /**
     * 누적 세미나 수를 수정합니다.
     */
    public void updateTotalSeminarCount(Integer totalSeminarCount) {
        this.totalSeminarCount = totalSeminarCount;
    }
}
