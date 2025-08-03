package com.gdschongik.gdsc.domain.event.domain;

import com.gdschongik.gdsc.domain.common.model.BaseEntity;
import com.gdschongik.gdsc.domain.common.vo.Period;
import jakarta.persistence.*;
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

    @Comment("RSVP 활성화 상태")
    @Enumerated(EnumType.STRING)
    private UsageStatus rsvpStatus;

    @Comment("뒤풀이 활성화 상태")
    @Enumerated(EnumType.STRING)
    private UsageStatus afterPartyStatus;

    @Comment("선입금 활성화 상태")
    @Enumerated(EnumType.STRING)
    private UsageStatus prePaymentStatus;

    @Builder(access = AccessLevel.PRIVATE)
    private Event(
            String name,
            String venue,
            String applicationDescription,
            Period applicationPeriod,
            UsageStatus rsvpStatus,
            UsageStatus afterPartyStatus,
            UsageStatus prePaymentStatus) {
        this.name = name;
        this.venue = venue;
        this.applicationDescription = applicationDescription;
        this.applicationPeriod = applicationPeriod;
        this.rsvpStatus = rsvpStatus;
        this.afterPartyStatus = afterPartyStatus;
        this.prePaymentStatus = prePaymentStatus;
    }

    public static Event create(
            String name,
            String venue,
            String applicationDescription,
            Period applicationPeriod,
            UsageStatus rsvpStatus,
            UsageStatus afterPartyStatus,
            UsageStatus prePaymentStatus) {
        return Event.builder()
                .name(name)
                .venue(venue)
                .applicationDescription(applicationDescription)
                .applicationPeriod(applicationPeriod)
                .rsvpStatus(rsvpStatus)
                .afterPartyStatus(afterPartyStatus)
                .prePaymentStatus(prePaymentStatus)
                .build();
    }
}
