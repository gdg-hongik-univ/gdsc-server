package com.gdschongik.gdsc.domain.coupon.application;

import com.gdschongik.gdsc.domain.study.domain.event.StudyHistoriesCompletedEvent;
import com.gdschongik.gdsc.domain.study.domain.event.StudyHistoryCompletionWithdrawnEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponEventHandler {

    private final CouponService couponService;

    @ApplicationModuleListener
    public void handleStudyHistoryCompletedEvent(StudyHistoriesCompletedEvent event) {
        log.info("[CouponEventHandler] 스터디 수료 이벤트 수신: studyHistoryIds={}", event.studyHistoryIds());
        couponService.createAndIssueCouponByStudyHistories(event.studyHistoryIds());
    }

    @ApplicationModuleListener
    public void handleStudyHistoryCompletionWithdrawnEvent(StudyHistoryCompletionWithdrawnEvent event) {
        log.info("[CouponEventHandler] 스터디 수료 철회 이벤트 수신: studyHistoryId={}", event.studyHistoryId());
        couponService.revokeStudyCompletionCouponByStudyHistoryId(event.studyHistoryId());
    }
}
