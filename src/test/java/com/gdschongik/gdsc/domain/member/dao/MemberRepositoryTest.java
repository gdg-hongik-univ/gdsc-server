package com.gdschongik.gdsc.domain.member.dao;

import static com.gdschongik.gdsc.domain.member.domain.Department.*;
import static com.gdschongik.gdsc.domain.member.domain.MemberRole.*;
import static com.gdschongik.gdsc.global.common.constant.MemberConstant.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.member.dto.request.MemberQueryOption;
import com.gdschongik.gdsc.helper.RepositoryTest;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

class MemberRepositoryTest extends RepositoryTest {

    private static final MemberQueryOption GUEST_QUERY_OPTION =
            new MemberQueryOption(null, null, null, null, null, null, null, List.of(GUEST));

    private static final MemberQueryOption ASSOCIATE_QUERY_OPTION =
            new MemberQueryOption(null, null, null, null, null, null, null, List.of(ASSOCIATE));

    @Autowired
    private MemberRepository memberRepository;

    private Member getMember() {
        Member member = Member.createGuest(OAUTH_ID);
        return memberRepository.save(member);
    }

    private void flushAndClearBeforeExecute() {
        testEntityManager.flush();
        testEntityManager.clear();
    }

    @Nested
    class 멤버_상태로_조회할때 {
        @Test
        void NORMAL이라면_조회_성공한다() {
            // given
            Member member = getMember();

            // when
            List<Member> members = memberRepository.findAll();

            // then
            assertThat(members).contains(member);
        }

        @Test
        void DELETED라면_조회되지_않는다() {
            // given
            Member member = getMember();
            member.withdraw();

            // when
            List<Member> members = memberRepository.findAll();

            // then
            assertThat(members).doesNotContain(member);
        }
    }

    @Nested
    class 역할로_조회할때 {

        @Test
        void 기본_회원정보_작성후_준회원_승급전_이라면_GUEST로_조회된다() {
            // given
            Member member = getMember();
            member.updateInfo(STUDENT_ID, NAME, PHONE_NUMBER, D022, UNIV_EMAIL);

            flushAndClearBeforeExecute();

            // when
            Page<Member> members = memberRepository.searchMembers(GUEST_QUERY_OPTION, PageRequest.of(0, 10));

            // then
            Member guest = memberRepository.findById(1L).get();
            assertThat(members).contains(guest);
        }

        @Test
        void 기본_회원정보_작성후_준회원_승급후라면_ASSOCIATE로_조회된다() {
            // given
            Member member = getMember();
            member.updateInfo(STUDENT_ID, NAME, PHONE_NUMBER, D022, UNIV_EMAIL);
            member.completeUnivEmailVerification(UNIV_EMAIL);
            member.verifyDiscord(DISCORD_USERNAME, NICKNAME, DISCORD_ID);
            member.advanceToAssociate();

            flushAndClearBeforeExecute();

            // when
            Page<Member> members = memberRepository.searchMembers(ASSOCIATE_QUERY_OPTION, PageRequest.of(0, 10));

            // then
            Member user = memberRepository.findById(1L).get();
            assertThat(members).contains(user);
        }

        @Test
        void 기본_회원정보_작성후_준회원_승급후라면_GUEST로_조회되지_않는다() {
            // given
            Member member = getMember();
            member.updateInfo(STUDENT_ID, NAME, PHONE_NUMBER, D022, UNIV_EMAIL);
            member.completeUnivEmailVerification(UNIV_EMAIL);
            member.verifyDiscord(DISCORD_USERNAME, NICKNAME, DISCORD_ID);
            member.advanceToAssociate();

            flushAndClearBeforeExecute();

            // when
            Page<Member> members = memberRepository.searchMembers(GUEST_QUERY_OPTION, PageRequest.of(0, 10));

            // then
            Member user = memberRepository.findById(1L).get();
            assertThat(members).doesNotContain(user);
        }
    }
}
