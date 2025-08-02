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

    @Builder(access = AccessLevel.PRIVATE)
    private EventParticipation(
            Participant participant,
            Long memberId,
            MainEventApplicationStatus mainEventApplicationStatus,
            AfterPartyAttendanceStatus mainEventAfterPartyAttendanceStatus,
            AfterPartyApplicationStatus afterPartyApplicationStatus,
            AfterPartyAttendanceStatus afterPartyAttendanceStatus,
            PaymentStatus prePaymentStatus,
            PaymentStatus postPaymentStatus) {
        this.participant = participant;
        this.memberId = memberId;
        this.mainEventApplicationStatus = mainEventApplicationStatus;
        this.afterPartyApplicationStatus = afterPartyApplicationStatus;
        this.afterPartyAttendanceStatus = afterPartyAttendanceStatus;
        this.prePaymentStatus = prePaymentStatus;
        this.postPaymentStatus = postPaymentStatus;
    }

    // 신청 폼을 통해 생성된 참여정보

    public static EventParticipation createRegisteredByForm(
            Member member, AfterPartyApplicationStatus afterPartyApplicationStatus) {
        return EventParticipation.builder()
                .participant(Participant.from(member))
                .memberId(member.getId())
                .mainEventApplicationStatus(MainEventApplicationStatus.APPLIED)
                .afterPartyApplicationStatus(afterPartyApplicationStatus)
                .afterPartyAttendanceStatus(AfterPartyAttendanceStatus.NONE)
                .prePaymentStatus(PaymentStatus.UNPAID)
                .postPaymentStatus(PaymentStatus.UNPAID)
                .build();
    }

    public static EventParticipation createUnregisteredByForm(
            Participant participant, AfterPartyApplicationStatus afterPartyApplicationStatus) {
        return EventParticipation.builder()
                .participant(participant)
                .mainEventApplicationStatus(MainEventApplicationStatus.APPLIED)
                .afterPartyApplicationStatus(afterPartyApplicationStatus)
                .afterPartyAttendanceStatus(AfterPartyAttendanceStatus.NOT_ATTENDED)
                .prePaymentStatus(PaymentStatus.UNPAID)
                .postPaymentStatus(PaymentStatus.UNPAID)
                .build();
    }

    // 뒷풀이 현장등록을 통해 생성된 참여정보

    public static EventParticipation createRegisteredByAfterParty(Member member) {
        return EventParticipation.builder()
                .participant(Participant.from(member))
                .memberId(member.getId())
                .mainEventApplicationStatus(MainEventApplicationStatus.NOT_APPLIED)
                .afterPartyApplicationStatus(AfterPartyApplicationStatus.NOT_APPLIED)
                .afterPartyAttendanceStatus(AfterPartyAttendanceStatus.ATTENDED)
                .prePaymentStatus(PaymentStatus.UNPAID)
                .postPaymentStatus(PaymentStatus.UNPAID)
                .build();
    }

    public static EventParticipation createUnregisteredByAfterParty(Participant participant) {
        return EventParticipation.builder()
                .participant(participant)
                .mainEventApplicationStatus(MainEventApplicationStatus.NOT_APPLIED)
                .afterPartyApplicationStatus(AfterPartyApplicationStatus.NOT_APPLIED)
                .afterPartyAttendanceStatus(AfterPartyAttendanceStatus.ATTENDED)
                .prePaymentStatus(PaymentStatus.UNPAID)
                .postPaymentStatus(PaymentStatus.UNPAID)
                .build();
    }
}
