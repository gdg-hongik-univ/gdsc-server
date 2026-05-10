package com.gdschongik.gdsc.domain.membership.application;

import com.gdschongik.gdsc.domain.order.domain.event.OrderCanceledEvent;
import com.gdschongik.gdsc.domain.order.domain.event.OrderCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MembershipEventHandler {

    private final MembershipService membershipService;

    @ApplicationModuleListener
    public void handleOrderCompletedEvent(OrderCompletedEvent orderCompletedEvent) {
        log.info("[MembershipEventHandler] 주문 완료 이벤트 수신: nanoId={}", orderCompletedEvent.nanoId());
        membershipService.verifyPaymentStatus(orderCompletedEvent.nanoId());
    }

    @ApplicationModuleListener
    public void handleOrderCanceledEvent(OrderCanceledEvent orderCanceledEvent) {
        log.info("[MembershipEventHandler] 주문 취소 이벤트 수신: orderId={}", orderCanceledEvent.orderId());
        membershipService.revokePaymentStatus(orderCanceledEvent.orderId());
    }
}
