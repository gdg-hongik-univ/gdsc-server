package com.gdschongik.gdsc.domain.coupon.application;

import com.gdschongik.gdsc.domain.study.domain.event.StudyHistoriesCompletedEvent;
import com.gdschongik.gdsc.domain.study.domain.event.StudyHistoryCompletionWithdrawnEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponEventHandler {
    // TODO: 여기서는 쿠폰 외 도메인의 이벤트를 받아서 쿠폰 서비스를 호출. 다른 핸들러는 반대로 되어있으므로 수정 필요

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
