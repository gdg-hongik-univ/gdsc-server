package com.gdschongik.gdsc.domain.statistic.application;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommunityStatisticScheduler {

    private final CommunityStatisticService communityStatisticService;
    private static final String CRON = "0 0 3 * * 1";

    @Scheduled(cron = CRON)
    private void updateCommunityStatistic() {
        communityStatisticService.updateCommunityStatistic();
    }
}
