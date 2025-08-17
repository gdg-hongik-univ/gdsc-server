package com.gdschongik.gdsc.domain.event.dto.dto;

@Deprecated
public record ParticipantDto(String name, String studentId, String phone) {
    // TODO: DTO 대신 VO를 사용하도록 변경 후 제거
}
