package com.gdschongik.gdsc.domain.statistic.dto.response;

import com.gdschongik.gdsc.domain.statistic.domain.CommunityStatistic;

public record CommunityStatisticResponse(
        Integer totalMemberCount, Integer totalProjectCount, Integer totalSeminarCount) {

    public static CommunityStatisticResponse from(CommunityStatistic communityStatistic) {
        return new CommunityStatisticResponse(
                communityStatistic.getTotalMemberCount(),
                communityStatistic.getTotalProjectCount(),
                communityStatistic.getTotalSeminarCount());
    }
}
