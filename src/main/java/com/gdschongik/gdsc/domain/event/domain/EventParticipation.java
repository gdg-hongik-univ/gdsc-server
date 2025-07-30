package com.gdschongik.gdsc.domain.event.domain;

import com.gdschongik.gdsc.domain.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Enumerated(EnumType.STRING)
    private EventApplicationStatus mainEventApplicationStatus;

    @Enumerated(EnumType.STRING)
    private EventAttendanceStatus mainEventAttendanceStatus;

    @Enumerated(EnumType.STRING)
    private EventApplicationStatus afterPartyApplicationStatus;

    @Enumerated(EnumType.STRING)
    private EventAttendanceStatus afterPartyAttendanceStatus;
}
