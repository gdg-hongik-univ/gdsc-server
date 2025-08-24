package com.gdschongik.gdsc.domain.event.domain;

import com.gdschongik.gdsc.domain.common.model.BaseEntity;
import com.gdschongik.gdsc.domain.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Getter
@Entity
@Table(
        name = "event_participation",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"event_id", "member_id"})})
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

    @Comment("뒤풀이 신청 상태")
    @Enumerated(EnumType.STRING)
    private AfterPartyApplicationStatus afterPartyApplicationStatus;

    @Comment("뒤풀이 참석 상태")
    @Enumerated(EnumType.STRING)
    private AfterPartyAttendanceStatus afterPartyAttendanceStatus;

    @Comment("뒤풀이 선입금 납부 상태")
    @Enumerated(EnumType.STRING)
    private PaymentStatus prePaymentStatus;

    @Comment("뒤풀이 후정산 납부 상태")
    @Enumerated(EnumType.STRING)
    private PaymentStatus postPaymentStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @Builder(access = AccessLevel.PRIVATE)
    private EventParticipation(
            Participant participant,
            Long memberId,
            MainEventApplicationStatus mainEventApplicationStatus,
            AfterPartyApplicationStatus afterPartyApplicationStatus,
            AfterPartyAttendanceStatus afterPartyAttendanceStatus,
            PaymentStatus prePaymentStatus,
            PaymentStatus postPaymentStatus,
            Event event) {
        this.participant = participant;
        this.memberId = memberId;
        this.mainEventApplicationStatus = mainEventApplicationStatus;
        this.afterPartyApplicationStatus = afterPartyApplicationStatus;
        this.afterPartyAttendanceStatus = afterPartyAttendanceStatus;
        this.prePaymentStatus = prePaymentStatus;
        this.postPaymentStatus = postPaymentStatus;
        this.event = event;
    }

    // 신청 폼을 통해 생성된 참여정보

    public static EventParticipation createOnlineForRegistered(
            Member member,
            AfterPartyApplicationStatus afterPartyApplicationStatus,
            AfterPartyAttendanceStatus afterPartyAttendanceStatus,
            PaymentStatus prePaymentStatus,
            PaymentStatus postPaymentStatus,
            Event event) {
        return EventParticipation.builder()
                .participant(Participant.from(member))
                .memberId(member.getId())
                .mainEventApplicationStatus(MainEventApplicationStatus.APPLIED)
                .afterPartyApplicationStatus(afterPartyApplicationStatus)
                .afterPartyAttendanceStatus(afterPartyAttendanceStatus)
                .prePaymentStatus(prePaymentStatus)
                .postPaymentStatus(postPaymentStatus)
                .event(event)
                .build();
    }

    public static EventParticipation createOnlineForUnregistered(
            Participant participant,
            AfterPartyApplicationStatus afterPartyApplicationStatus,
            AfterPartyAttendanceStatus afterPartyAttendanceStatus,
            PaymentStatus prePaymentStatus,
            PaymentStatus postPaymentStatus,
            Event event) {
        return EventParticipation.builder()
                .participant(participant)
                .mainEventApplicationStatus(MainEventApplicationStatus.APPLIED)
                .afterPartyApplicationStatus(afterPartyApplicationStatus)
                .afterPartyAttendanceStatus(afterPartyAttendanceStatus)
                .prePaymentStatus(prePaymentStatus)
                .postPaymentStatus(postPaymentStatus)
                .event(event)
                .build();
    }

    // 뒤풀이 현장등록을 통해 생성된 참여정보

    public static EventParticipation createOnsiteForRegistered(
            Member member, PaymentStatus prePaymentStatus, PaymentStatus postPaymentStatus, Event event) {
        return EventParticipation.builder()
                .participant(Participant.from(member))
                .memberId(member.getId())
                .mainEventApplicationStatus(MainEventApplicationStatus.NOT_APPLIED)
                .afterPartyApplicationStatus(AfterPartyApplicationStatus.NOT_APPLIED)
                .afterPartyAttendanceStatus(AfterPartyAttendanceStatus.ATTENDED)
                .prePaymentStatus(prePaymentStatus)
                .postPaymentStatus(postPaymentStatus)
                .event(event)
                .build();
    }

    public static EventParticipation createOnsiteForUnregistered(
            Participant participant, PaymentStatus prePaymentStatus, PaymentStatus postPaymentStatus, Event event) {
        return EventParticipation.builder()
                .participant(participant)
                .mainEventApplicationStatus(MainEventApplicationStatus.NOT_APPLIED)
                .afterPartyApplicationStatus(AfterPartyApplicationStatus.NOT_APPLIED)
                .afterPartyAttendanceStatus(AfterPartyAttendanceStatus.ATTENDED)
                .prePaymentStatus(prePaymentStatus)
                .postPaymentStatus(postPaymentStatus)
                .event(event)
                .build();
    }

    // 뒤풀이 정산 처리
    public void checkPostPayment() {
        this.postPaymentStatus = PaymentStatus.PAID;
    }
}
