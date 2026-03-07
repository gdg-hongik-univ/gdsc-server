package com.gdschongik.gdsc.domain.membership.dao;

import static com.gdschongik.gdsc.domain.membership.domain.QMembership.*;

import com.gdschongik.gdsc.domain.common.model.RequirementStatus;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.recruitment.domain.Recruitment;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MembershipCustomRepositoryImpl implements MembershipCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public boolean existsSatisfiedMembershipByRecruitment(Member member, Recruitment recruitment) {
        Integer fetchOne = queryFactory
                .selectOne()
                .from(membership)
                .where(
                        eqMember(member),
                        eqRecruitment(recruitment),
                        eqRequirementStatus(membership.regularRequirement.paymentStatus, RequirementStatus.SATISFIED))
                .fetchFirst();

        return fetchOne != null;
    }

    private BooleanExpression eqMember(Member member) {
        return member != null ? membership.member.eq(member) : null;
    }

    private BooleanExpression eqRecruitment(Recruitment recruitment) {
        return recruitment != null ? membership.recruitmentRound.recruitment.eq(recruitment) : null;
    }

    private BooleanExpression eqRequirementStatus(
            EnumPath<RequirementStatus> requirement, RequirementStatus requirementStatus) {
        return requirementStatus != null ? requirement.eq(requirementStatus) : null;
    }
}
