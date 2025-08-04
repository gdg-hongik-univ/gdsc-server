package com.gdschongik.gdsc.domain.event.domain;

import static com.gdschongik.gdsc.domain.event.domain.UsageStatus.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.common.model.BaseEntity;
import com.gdschongik.gdsc.domain.common.vo.Period;
import com.gdschongik.gdsc.global.exception.CustomException;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;

    @Comment("행사 이름")
    private String name;

    @Comment("행사 진행 장소")
    private String venue;

    @Comment("행사 신청 폼 설명")
    @Column(columnDefinition = "TEXT")
    private String applicationDescription;

    @Comment("행사 신청 기간")
    @Embedded
    @AttributeOverride(name = "startDate", column = @Column(name = "application_start_at"))
    @AttributeOverride(name = "endDate", column = @Column(name = "application_end_at"))
    private Period applicationPeriod;

    /**
     * 정회원만 신청 폼 작성을 허용할 것인지 여부를 나타내는 필드입니다.
     */
    @Comment("정회원 전용 신청 폼 여부")
    @Enumerated(EnumType.STRING)
    private UsageStatus regularRoleOnlyStatus;

    /**
     * 뒤풀이 진행 여부를 나타내는 필드입니다.
     */
    @Comment("뒤풀이 활성화 상태")
    @Enumerated(EnumType.STRING)
    private UsageStatus afterPartyStatus;

    @Comment("뒤풀이 선입금 활성화 상태")
    @Enumerated(EnumType.STRING)
    private UsageStatus prePaymentStatus;

    @Comment("뒤풀이 후정산 활성화 상태")
    @Enumerated(EnumType.STRING)
    private UsageStatus postPaymentStatus;

    /**
     * 신청 폼에 RSVP 요청 리마인더 질문 포함 여부를 나타내는 필드입니다. 어떠한 검증에도 활용되지 않습니다.
     */
    @Comment("RSVP 질문 활성화 상태")
    @Enumerated(EnumType.STRING)
    private UsageStatus rsvpQuestionStatus;

    @Comment("본행사 최대 신청 가능 인원")
    private Integer mainEventMaxApplicantCount;

    @Comment("뒤풀이 최대 신청 가능 인원")
    private Integer afterPartyMaxApplicantCount;

    @Builder(access = AccessLevel.PRIVATE)
    private Event(
            String name,
            String venue,
            String applicationDescription,
            Period applicationPeriod,
            UsageStatus regularRoleOnlyStatus,
            UsageStatus afterPartyStatus,
            UsageStatus prePaymentStatus,
            UsageStatus postPaymentStatus,
            UsageStatus rsvpQuestionStatus,
            Integer mainEventMaxApplicantCount,
            Integer afterPartyMaxApplicantCount) {
        this.name = name;
        this.venue = venue;
        this.applicationDescription = applicationDescription;
        this.applicationPeriod = applicationPeriod;
        this.regularRoleOnlyStatus = regularRoleOnlyStatus;
        this.afterPartyStatus = afterPartyStatus;
        this.prePaymentStatus = prePaymentStatus;
        this.postPaymentStatus = postPaymentStatus;
        this.rsvpQuestionStatus = rsvpQuestionStatus;
        this.mainEventMaxApplicantCount = mainEventMaxApplicantCount;
        this.afterPartyMaxApplicantCount = afterPartyMaxApplicantCount;
    }

    // 생성 팩토리 메서드
    public static Event create(
            String name,
            String venue,
            String applicationDescription,
            Period applicationPeriod,
            UsageStatus regularRoleOnlyStatus,
            UsageStatus afterPartyStatus,
            UsageStatus prePaymentStatus,
            UsageStatus postPaymentStatus,
            UsageStatus rsvpQuestionStatus,
            Integer mainEventMaxApplicantCount,
            Integer afterPartyMaxApplicantCount) {
        validatePaymentDisabledWhenAfterPartyDisabled(afterPartyStatus, prePaymentStatus, postPaymentStatus);
        validatePrePaymentAndPostPayment(prePaymentStatus, postPaymentStatus);

        return Event.builder()
                .name(name)
                .venue(venue)
                .applicationDescription(applicationDescription)
                .applicationPeriod(applicationPeriod)
                .regularRoleOnlyStatus(regularRoleOnlyStatus)
                .afterPartyStatus(afterPartyStatus)
                .prePaymentStatus(prePaymentStatus)
                .postPaymentStatus(postPaymentStatus)
                .rsvpQuestionStatus(rsvpQuestionStatus)
                .mainEventMaxApplicantCount(mainEventMaxApplicantCount)
                .afterPartyMaxApplicantCount(afterPartyMaxApplicantCount)
                .build();
    }

    // 검증 메서드
    private static void validatePaymentDisabledWhenAfterPartyDisabled(
            UsageStatus afterPartyStatus, UsageStatus prePaymentStatus, UsageStatus postPaymentStatus) {
        if (afterPartyStatus == DISABLED && (prePaymentStatus == ENABLED || postPaymentStatus == ENABLED)) {
            throw new CustomException(EVENT_NOT_CREATABLE_PAYMENT_STATUS_ENABLED);
        }
    }

    private static void validatePrePaymentAndPostPayment(UsageStatus prePaymentStatus, UsageStatus postPaymentStatus) {
        if (prePaymentStatus == ENABLED && postPaymentStatus == ENABLED) {
            throw new CustomException(EVENT_NOT_CREATABLE_PAYMENTS_BOTH_ENABLED);
        }
    }

    // 정보 조회 메서드
    public boolean isAppliable(LocalDateTime now) {
        return applicationPeriod.isWithin(now);
    }
}
