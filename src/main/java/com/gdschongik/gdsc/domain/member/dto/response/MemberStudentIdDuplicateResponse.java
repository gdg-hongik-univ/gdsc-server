package com.gdschongik.gdsc.domain.member.dto.response;

public record MemberStudentIdDuplicateResponse(boolean isDuplicate) {
    public static MemberStudentIdDuplicateResponse from(boolean isDuplicate) {
        return new MemberStudentIdDuplicateResponse(isDuplicate);
    }
}
