package com.gdschongik.gdsc.domain.event.dto.dto;

import com.gdschongik.gdsc.domain.member.domain.Member;

public record EventParticipatableMemberDto(Long memberId, String name, String studentId, boolean participatable) {
    public static EventParticipatableMemberDto from(Member member, boolean participatable) {
        return new EventParticipatableMemberDto(
                member.getId(), member.getName(), member.getStudentId(), participatable);
    }
}
