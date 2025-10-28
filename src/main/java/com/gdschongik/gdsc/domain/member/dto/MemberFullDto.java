package com.gdschongik.gdsc.domain.member.dto;

import com.gdschongik.gdsc.domain.common.model.RequirementStatus;
import com.gdschongik.gdsc.domain.member.domain.Department;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.member.domain.MemberManageRole;
import com.gdschongik.gdsc.domain.member.domain.MemberRole;
import com.gdschongik.gdsc.domain.member.domain.MemberStudyRole;
import com.gdschongik.gdsc.global.util.formatter.PhoneFormatter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Optional;

public record MemberFullDto(
        Long memberId,
        @Schema(description = "멤버 역할", implementation = MemberRole.class) MemberRole role,
        @Schema(description = "멤버 관리자 역할", implementation = MemberManageRole.class) MemberManageRole manageRole,
        @Schema(description = "멤버 스터디 역할", implementation = MemberStudyRole.class) MemberStudyRole studyRole,
        @Schema(description = "회원정보", implementation = MemberInfoDto.class) MemberInfoDto info,
        @Schema(description = "인증상태정보", implementation = MemberAssociateRequirementDto.class)
                MemberAssociateRequirementDto associateRequirement) {
    public static MemberFullDto of(Member member, UnivVerificationStatus univVerificationStatus) {
        return new MemberFullDto(
                member.getId(),
                member.getRole(),
                member.getManageRole(),
                member.getStudyRole(),
                MemberInfoDto.from(member),
                MemberAssociateRequirementDto.of(member, univVerificationStatus));
    }

    record MemberInfoDto(
            String name,
            String studentId,
            String email,
            String department,
            String phone,
            String discordUsername,
            String nickname) {
        public static MemberInfoDto from(Member member) {
            return new MemberInfoDto(
                    member.getName(),
                    member.getStudentId(),
                    member.getEmail(),
                    Optional.ofNullable(member.getDepartment())
                            .map(Department::getDepartmentName)
                            .orElse(null),
                    Optional.ofNullable(member.getPhone())
                            .map(PhoneFormatter::format)
                            .orElse(null),
                    member.getDiscordUsername(),
                    member.getNickname());
        }
    }

    public record MemberAssociateRequirementDto(
            @Schema(description = "학교메일 인증상태", implementation = UnivVerificationStatus.class)
                    UnivVerificationStatus univStatus,
            @Schema(description = "디스코드 인증상태", implementation = RequirementStatus.class)
                    RequirementStatus discordStatus,
            @Schema(description = "회원정보 입력상태", implementation = RequirementStatus.class) RequirementStatus infoStatus) {
        public static MemberAssociateRequirementDto of(Member member, UnivVerificationStatus univVerificationStatus) {
            return new MemberAssociateRequirementDto(
                    univVerificationStatus,
                    member.getAssociateRequirement().getDiscordStatus(),
                    member.getAssociateRequirement().getInfoStatus());
        }
    }
}
