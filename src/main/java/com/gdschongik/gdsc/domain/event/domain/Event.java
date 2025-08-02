package com.gdschongik.gdsc.domain.event.domain;

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
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private EventType type;

    @Comment("행사 이름")
    private String name;

    @Comment("행사 설명")
    @Column(columnDefinition = "TEXT")
    private String description;

    @Comment("행사 기간")
    @Embedded
    @AttributeOverride(name = "startDate", column = @Column(name = "event_start_at"))
    @AttributeOverride(name = "endDate", column = @Column(name = "event_end_at"))
    private Period period;

    @Comment("RSVP 활성화 여부")
    @Enumerated(EnumType.STRING)
    private UsageStatus rsvp;

    @Comment("뒤풀이 활성화 여부")
    @Enumerated(EnumType.STRING)
    private UsageStatus afterParty;

    @Comment("선입금 활성화 여부")
    @Enumerated(EnumType.STRING)
    private UsageStatus prePayment;

    @Builder(access = AccessLevel.PRIVATE)
    private Event(
            EventType type,
            String name,
            String description,
            Period period,
            UsageStatus rsvp,
            UsageStatus afterParty,
            UsageStatus prePayment) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.period = period;
        this.rsvp = rsvp;
        this.afterParty = afterParty;
        this.prePayment = prePayment;
    }

    public static Event create(
            EventType type,
            String name,
            String description,
            Period period,
            UsageStatus rsvp,
            UsageStatus afterParty,
            UsageStatus prePayment) {
        return Event.builder()
                .type(type)
                .name(name)
                .description(description)
                .period(period)
                .rsvp(rsvp)
                .afterParty(afterParty)
                .prePayment(prePayment)
                .build();
    }
}
