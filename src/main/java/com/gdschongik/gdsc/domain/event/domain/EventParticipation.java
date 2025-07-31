package com.gdschongik.gdsc.domain.event.domain;

import com.gdschongik.gdsc.domain.common.model.BaseEntity;
import com.gdschongik.gdsc.domain.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventParticipation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_participation_id")
    private Long id;

    @Embedded
    private Participant participant;

    private Long memberId;

    @Comment("본행사 신청 상태")
    @Enumerated(EnumType.STRING)
    private MainEventApplicationStatus mainEventApplicationStatus;

    @Comment("뒷풀이 신청 상태")
    @Enumerated(EnumType.STRING)
    private AfterPartyApplicationStatus afterPartyApplicationStatus;

    @Comment("뒷풀이 참석 상태")
    @Enumerated(EnumType.STRING)
    private AfterPartyAttendanceStatus afterPartyAttendanceStatus;

    @Comment("선정산 납부 상태")
    @Enumerated(EnumType.STRING)
    private PaymentStatus prePaymentStatus;

    @Comment("후정산 납부 상태")
    @Enumerated(EnumType.STRING)
    private PaymentStatus postPaymentStatus;
}
