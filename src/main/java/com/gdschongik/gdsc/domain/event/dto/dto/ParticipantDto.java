package com.gdschongik.gdsc.domain.event.dto.dto;

import com.gdschongik.gdsc.domain.event.domain.Participant;

public record ParticipantDto(String name, String studentId, String phone) {
    public static ParticipantDto from(Participant participant) {
        return new ParticipantDto(participant.getName(), participant.getStudentId(), participant.getPhone());
    }
}
