package com.gdschongik.gdsc.domain.event.dto.dto;

import com.gdschongik.gdsc.domain.member.domain.Member;

public record EventParticipableMemberDto(Long memberId, String name, String studentId, boolean participable) {
    public static EventParticipableMemberDto from(Member member, boolean participable) {
        return new EventParticipableMemberDto(member.getId(), member.getName(), member.getStudentId(), participable);
    }
}
