package com.gdschongik.gdsc.helper;

import static com.gdschongik.gdsc.domain.member.domain.Department.*;
import static com.gdschongik.gdsc.domain.member.domain.MemberManageRole.ADMIN;
import static com.gdschongik.gdsc.domain.member.domain.MemberStudyRole.MENTOR;
import static com.gdschongik.gdsc.global.common.constant.CouponConstant.*;
import static com.gdschongik.gdsc.global.common.constant.EventConstant.*;
import static com.gdschongik.gdsc.global.common.constant.MemberConstant.*;
import static com.gdschongik.gdsc.global.common.constant.RecruitmentConstant.*;
import static com.gdschongik.gdsc.global.common.constant.StudyConstant.*;
import static com.gdschongik.gdsc.global.common.constant.TemporalConstant.*;

import com.gdschongik.gdsc.domain.common.model.BaseEntity;
import com.gdschongik.gdsc.domain.common.model.SemesterType;
import com.gdschongik.gdsc.domain.common.vo.Money;
import com.gdschongik.gdsc.domain.common.vo.Period;
import com.gdschongik.gdsc.domain.common.vo.Semester;
import com.gdschongik.gdsc.domain.coupon.domain.Coupon;
import com.gdschongik.gdsc.domain.coupon.domain.CouponType;
import com.gdschongik.gdsc.domain.coupon.domain.IssuedCoupon;
import com.gdschongik.gdsc.domain.event.domain.Event;
import com.gdschongik.gdsc.domain.event.domain.UsageStatus;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.membership.domain.Membership;
import com.gdschongik.gdsc.domain.recruitment.domain.Recruitment;
import com.gdschongik.gdsc.domain.recruitment.domain.RecruitmentRound;
import com.gdschongik.gdsc.domain.recruitment.domain.RoundType;
import com.gdschongik.gdsc.domain.study.domain.Study;
import com.gdschongik.gdsc.domain.study.domain.StudyFactory;
import com.gdschongik.gdsc.domain.study.domain.StudyType;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.test.util.ReflectionTestUtils;

public class FixtureHelper {

    StudyFactory studyFactory = new StudyFactory();

    public <T extends BaseEntity> void setId(T entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
    }

    public Member createGuestMember(Long id) {
        Member member = Member.createGuest(OAUTH_ID);
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    public Member createAssociateMember(Long id) {
        Member member = createGuestMember(id);
        member.updateInfo(STUDENT_ID, NAME, PHONE_NUMBER, D022, EMAIL);
        member.completeUnivEmailVerification(UNIV_EMAIL);
        member.verifyDiscord(DISCORD_USERNAME, NICKNAME, DISCORD_ID);
        member.advanceToAssociate();
        return member;
    }

    public Member createRegularMember(Long id) {
        Member member = createAssociateMember(id);
        member.advanceToRegular();
        return member;
    }

    public Member createAdmin(Long id) {
        Member member = createRegularMember(id);
        ReflectionTestUtils.setField(member, "manageRole", ADMIN);
        return member;
    }

    public Member createMentor(Long id) {
        Member member = createRegularMember(id);
        member.assignToMentor();
        ReflectionTestUtils.setField(member, "studyRole", MENTOR);
        return member;
    }

    public RecruitmentRound createRecruitmentRound(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Integer academicYear,
            SemesterType semesterType,
            Money fee) {
        Recruitment recruitment = Recruitment.create(
                FEE_NAME,
                fee,
                Period.of(SEMESTER_START_DATE, SEMESTER_END_DATE),
                Semester.of(academicYear, semesterType));

        return RecruitmentRound.create(
                RECRUITMENT_ROUND_NAME, RoundType.FIRST, Period.of(startDate, endDate), recruitment);
    }

    public Membership createMembership(Member member, RecruitmentRound recruitmentRound) {
        return Membership.create(member, recruitmentRound);
    }

    public IssuedCoupon createAndIssue(Money money, Member member, CouponType couponType, Study study) {
        Coupon coupon = Coupon.createAutomatic(COUPON_NAME, money, couponType, study);
        return IssuedCoupon.create(coupon, member);
    }

    public Study createStudy(StudyType type, Long studyId, Long firstStudySessionId, Long mentorId) {
        Study study = studyFactory.create(
                type,
                STUDY_TITLE,
                STUDY_SEMESTER,
                TOTAL_ROUND,
                DAY_OF_WEEK,
                STUDY_START_TIME,
                STUDY_END_TIME,
                STUDY_APPLICATION_PERIOD,
                STUDY_DISCORD_CHANNEL_ID,
                STUDY_DISCORD_ROLE_ID,
                createMentor(mentorId),
                () -> "0000",
                MIN_ASSIGNMENT_CONTENT_LENGTH);

        setId(study, studyId);

        AtomicLong currentStudySessionId = new AtomicLong(firstStudySessionId);
        study.getStudySessions().forEach(session -> setId(session, currentStudySessionId.getAndIncrement()));

        return study;
    }

    public Event createEventWithAfterParty(Long id, UsageStatus regularRoleOnlyStatus) {
        Event event = Event.create(
                EVENT_NAME,
                VENUE,
                EVENT_START_AT,
                EVENT_DESCRIPTION,
                EVENT_APPLICATION_PERIOD,
                regularRoleOnlyStatus,
                MAIN_EVENT_MAX_APPLICATION_COUNT,
                AFTER_PARTY_MAX_APPLICATION_COUNT);

        setId(event, id);
        return event;
    }

    public Event createEventWithoutAfterParty(Long id) {
        Event event = Event.create(
                EVENT_NAME,
                VENUE,
                EVENT_START_AT,
                EVENT_DESCRIPTION,
                EVENT_APPLICATION_PERIOD,
                REGULAR_ROLE_ONLY_STATUS,
                MAIN_EVENT_MAX_APPLICATION_COUNT,
                AFTER_PARTY_MAX_APPLICATION_COUNT);

        setId(event, id);
        ReflectionTestUtils.setField(event, "afterPartyStatus", UsageStatus.DISABLED);
        return event;
    }
}
