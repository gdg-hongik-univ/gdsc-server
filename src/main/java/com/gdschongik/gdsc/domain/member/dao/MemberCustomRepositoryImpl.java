package com.gdschongik.gdsc.domain.member.dao;

import static com.gdschongik.gdsc.domain.common.model.RequirementStatus.*;
import static com.gdschongik.gdsc.domain.member.domain.QMember.*;
import static com.gdschongik.gdsc.domain.membership.domain.QMembership.*;
import static com.gdschongik.gdsc.domain.recruitment.domain.QRecruitment.*;
import static com.gdschongik.gdsc.domain.recruitment.domain.QRecruitmentRound.*;

import com.gdschongik.gdsc.domain.common.model.RequirementStatus;
import com.gdschongik.gdsc.domain.common.vo.Semester;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.member.domain.MemberRole;
import com.gdschongik.gdsc.domain.member.dto.request.MemberQueryOption;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.annotation.Nullable;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class MemberCustomRepositoryImpl implements MemberCustomRepository, MemberQueryMethod {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Member> searchMembers(MemberQueryOption queryOption, Pageable pageable) {

        List<Long> ids = getIdsByQueryOption(queryOption, null, member.createdAt.desc());

        List<Member> fetch = queryFactory
                .selectFrom(member)
                .where(member.id.in(ids))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return PageableExecutionUtils.getPage(fetch, pageable, ids::size);
    }

    @Override
    public List<Member> findAllByRole(MemberRole role) {
        return queryFactory
                .selectFrom(member)
                .where(eqRole(role))
                .orderBy(member.studentId.asc(), member.name.asc())
                .fetch();
    }

    @Override
    public List<Member> findAllByDiscordStatus(RequirementStatus discordStatus) {
        return queryFactory
                .selectFrom(member)
                .where(eqRequirementStatus(member.associateRequirement.discordStatus, discordStatus))
                .fetch();
    }

    @Override
    public List<Member> findAllAdvanceFailedMembers(@NonNull Semester semester) {
        return queryFactory
                .selectFrom(member)
                .leftJoin(membership)
                .on(membership.member.eq(member))
                .leftJoin(recruitmentRound)
                .on(membership.recruitmentRound.eq(recruitmentRound))
                .leftJoin(recruitment)
                .on(recruitmentRound.recruitment.eq(recruitment))
                .where(
                        eqRole(MemberRole.ASSOCIATE),
                        eqSemester(semester),
                        membership.regularRequirement.paymentStatus.eq(SATISFIED))
                .fetch();
    }

    @Override
    public boolean existsInfoStatusSatisfiedMemberByStudentId(String studentId) {
        return queryFactory
                        .selectOne()
                        .from(member)
                        .where(
                                eqStudentId(studentId),
                                eqRequirementStatus(member.associateRequirement.infoStatus, SATISFIED))
                        .fetchFirst()
                != null;
    }

    /**
     * queryOption으로 정렬된 상태로id값들을 가져옵니다.
     * 이 id값들로 페이지네이션 content를 조인하는 쿼리 생성시 추가적인 정렬은 없어야하며, 정렬이 필요한경우 해당 함수에 넣어주세요.
     * @param queryOption -> 필수
     * @param predicate -> 옵션(추가적인 조건 있을 시)
     * @param orderSpecifiers -> 최소 1개 이상
     * @return
     */
    private List<Long> getIdsByQueryOption(
            MemberQueryOption queryOption,
            @Nullable Predicate predicate,
            @NonNull OrderSpecifier<?>... orderSpecifiers) {
        return queryFactory
                .select(member.id)
                .from(member)
                .where(matchesQueryOption(queryOption), predicate)
                .orderBy(orderSpecifiers)
                .fetch();
    }

    private BooleanExpression eqSemester(Semester semester) {
        return semester != null
                ? recruitment
                        .semester
                        .academicYear
                        .eq(semester.getAcademicYear())
                        .and(recruitment.semester.semesterType.eq(semester.getSemesterType()))
                : null;
    }
}
