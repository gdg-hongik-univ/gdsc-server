package com.gdschongik.gdsc.domain.member.dao;

import com.gdschongik.gdsc.domain.common.model.RequirementStatus;
import com.gdschongik.gdsc.domain.common.vo.Semester;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.member.domain.MemberRole;
import com.gdschongik.gdsc.domain.member.dto.request.MemberQueryOption;
import jakarta.annotation.Nullable;
import java.util.List;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberCustomRepository {

    Page<Member> searchMembers(MemberQueryOption queryOption, Pageable pageable);

    List<Member> findAllByRole(@Nullable MemberRole role);

    List<Member> findAllByDiscordStatus(RequirementStatus discordStatus);

    List<Member> findAllAdvanceFailedMembers(@NonNull Semester semester);

    boolean existsInfoStatusSatisfiedMemberByStudentId(String studentId);
}
