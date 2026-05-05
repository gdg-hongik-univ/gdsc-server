package com.gdschongik.gdsc.domain.order.domain;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.common.model.BaseEntity;
import com.gdschongik.gdsc.domain.coupon.domain.IssuedCoupon;
import com.gdschongik.gdsc.domain.membership.domain.Membership;
import com.gdschongik.gdsc.domain.order.domain.event.OrderCanceledEvent;
import com.gdschongik.gdsc.domain.order.domain.event.OrderCompletedEvent;
import com.gdschongik.gdsc.domain.order.domain.event.OrderCreatedEvent;
import com.gdschongik.gdsc.domain.order.domain.service.OrderValidator;
import com.gdschongik.gdsc.global.exception.CustomException;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.ZonedDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Getter
@Table(name = "orders")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orders_id")
    private Long id;

    @Comment("주문상태")
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Comment("주문 nanoId")
    @Column(unique = true, length = 21)
    private String nanoId;

    @Comment("주문자 ID")
    private Long memberId;

    @Comment("주문 대상 멤버십 ID")
    private Long membershipId;

    @Comment("신청하려는 모집회차 ID")
    private Long recruitmentRoundId;

    @Comment("사용하려는 발급쿠폰 ID")
    private Long issuedCouponId;

    private String paymentKey;

    private ZonedDateTime approvedAt;

    private ZonedDateTime canceledAt;

    @Embedded
    private MoneyInfo moneyInfo;

    @Builder(access = AccessLevel.PRIVATE)
    private Order(
            OrderStatus status,
            String nanoId,
            Long memberId,
            Long membershipId,
            Long recruitmentRoundId,
            Long issuedCouponId,
            MoneyInfo moneyInfo) {
        this.status = status;
        this.nanoId = nanoId;
        this.memberId = memberId;
        this.membershipId = membershipId;
        this.recruitmentRoundId = recruitmentRoundId;
        this.issuedCouponId = issuedCouponId;
        this.moneyInfo = moneyInfo;

        registerEvent(new OrderCreatedEvent(nanoId, isFree()));
    }

    /**
     * 결제 요청 전 임시 주문을 생성합니다.
     * 쿠폰의 경우 사용 여부를 선택할 수 있습니다.
     */
    public static Order createPending(
            String nanoId, MoneyInfo moneyInfo, Membership membership, @Nullable IssuedCoupon issuedCoupon) {
        return Order.builder()
                .status(OrderStatus.PENDING)
                .nanoId(nanoId)
                .memberId(membership.getMember().getId())
                .membershipId(membership.getId())
                .recruitmentRoundId(membership.getRecruitmentRound().getId())
                .issuedCouponId(issuedCoupon != null ? issuedCoupon.getId() : null)
                .moneyInfo(moneyInfo)
                .build();
    }

    /**
     * 무료 주문을 생성합니다.
     * 결제가 필요하지 않으므로 주문 완료 상태로 생성됩니다.
     * 주문 완료 상태로 생성되므로 주문 완료 이벤트 {@link OrderCompletedEvent} 를 함께 발행합니다.
     */
    public static Order createFree(
            String nanoId, MoneyInfo moneyInfo, Membership membership, @Nullable IssuedCoupon issuedCoupon) {
        validateFreeOrder(moneyInfo);
        Order order = Order.builder()
                .status(OrderStatus.COMPLETED)
                .nanoId(nanoId)
                .memberId(membership.getMember().getId())
                .membershipId(membership.getId())
                .recruitmentRoundId(membership.getRecruitmentRound().getId())
                .issuedCouponId(issuedCoupon != null ? issuedCoupon.getId() : null)
                .moneyInfo(moneyInfo)
                .build();

        order.registerEvent(new OrderCompletedEvent(nanoId));

        return order;
    }

    private static void validateFreeOrder(MoneyInfo moneyInfo) {
        if (!moneyInfo.isFree()) {
            throw new CustomException(ORDER_FREE_FINAL_PAYMENT_NOT_ZERO);
        }
    }

    // 데이터 변경 로직

    /**
     * 주문을 완료 처리합니다.
     * 상태 변경 및 결제 관련 정보를 저장하며, 예외를 발생시키지 않습니다.
     * 이는 결제 승인 API 호출 후 완료 처리 과정에서 예외가 발생하는 것을 방지하기 위함입니다.
     * 실제 완료 처리 유효성에 대한 검증은 {@link OrderValidator#validateCompleteOrder}에서 수행합니다.
     */
    public void complete(String paymentKey, ZonedDateTime approvedAt) {
        this.status = OrderStatus.COMPLETED;
        this.paymentKey = paymentKey;
        this.approvedAt = approvedAt;

        registerEvent(new OrderCompletedEvent(nanoId));
    }

    /**
     * 주문을 취소 처리합니다.
     * 상태 변경 및 취소 시각을 저장하며, 예외를 발생시키지 않도록 외부 취소 요청 전에 validateCancelable을 호출합니다.
     */
    public void cancel(ZonedDateTime canceledAt) {
        validateCancelable();
        this.status = OrderStatus.CANCELED;
        this.canceledAt = canceledAt;

        registerEvent(new OrderCanceledEvent(id));
    }

    public void validateCancelable() {
        if (status != OrderStatus.COMPLETED) {
            throw new CustomException(ORDER_CANCEL_NOT_COMPLETED);
        }

        if (isFree()) {
            throw new CustomException(ORDER_CANCEL_FREE_ORDER);
        }
    }

    // 데이터 조회 로직

    public boolean isCompleted() {
        return status == OrderStatus.COMPLETED;
    }

    public boolean isFree() {
        return moneyInfo.isFree();
    }
}
