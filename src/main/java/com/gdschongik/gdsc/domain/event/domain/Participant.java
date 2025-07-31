package com.gdschongik.gdsc.domain.event.domain;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.global.exception.CustomException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Participant(String name, @Column(unique = true) String studentId, String phone) {

    public static Participant of(String name, String studentId, String phone) {
        return new Participant(name, studentId, phone);
    }

    public static Participant from(Member member) {
        if (!member.getAssociateRequirement().isInfoSatisfied()) {
            throw new CustomException(PARTICIPANT_NOT_CREATABLE_INFO_NOT_SATISFIED);
        }

        return new Participant(member.getName(), member.getStudentId(), member.getPhone());
    }
}
