package com.gdschongik.gdsc.domain.member.application;

import static com.gdschongik.gdsc.global.common.constant.TestMemberConstant.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.member.dao.MemberRepository;
import com.gdschongik.gdsc.domain.member.domain.Department;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.member.dto.request.MemberUpdateRequest;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.exception.ErrorCode;
import com.gdschongik.gdsc.helper.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AdminMemberServiceTest extends IntegrationTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AdminMemberService adminMemberService;

    @Test
    void status가_DELETED라면_예외_발생() {
        // given
        Member member = Member.createGuest(OAUTH_ID);
        member.withdraw();
        memberRepository.save(member);

        // when & then
        MemberUpdateRequest requestBody = new MemberUpdateRequest(
                "A111111", "name", "010-1234-5678", Department.D001, "email@email.com", "discordUsername", "한글");
        assertThatThrownBy(() -> adminMemberService.updateMember(member.getId(), requestBody))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }
}
