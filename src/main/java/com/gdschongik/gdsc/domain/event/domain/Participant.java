package com.gdschongik.gdsc.domain.event.domain;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.global.exception.CustomException;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class Participant {

    private String name;

    private String studentId;

    private String phone;

    @Builder(access = AccessLevel.PRIVATE)
    private Participant(String name, String studentId, String phone) {
        this.name = name;
        this.studentId = studentId;
        this.phone = phone;
    }

    public static Participant of(String name, String studentId, String phone) {
        return Participant.builder()
                .name(name)
                .studentId(studentId)
                .phone(phone)
                .build();
    }

    public static Participant from(Member member) {
        if (!member.getAssociateRequirement().isInfoSatisfied()) {
            throw new CustomException(PARTICIPANT_NOT_CREATABLE_INFO_NOT_SATISFIED);
        }

        return Participant.builder()
                .name(member.getName())
                .studentId(member.getStudentId())
                .phone(member.getPhone())
                .build();
    }
}
