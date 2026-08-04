package com.gdschongik.gdsc.domain.order.dao;

import static com.gdschongik.gdsc.domain.member.domain.QMember.*;
import static com.gdschongik.gdsc.domain.order.domain.QOrder.*;
import static com.gdschongik.gdsc.domain.recruitment.domain.QRecruitment.*;
import static com.gdschongik.gdsc.domain.recruitment.domain.QRecruitmentRound.*;
import static com.querydsl.jpa.JPAExpressions.*;

import com.gdschongik.gdsc.domain.common.model.SemesterType;
import com.gdschongik.gdsc.domain.order.dto.request.OrderQueryOption;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import java.time.*;

public interface OrderQueryMethod {

    default BooleanBuilder matchesOrderQueryOption(OrderQueryOption queryOption) {
        return new BooleanBuilder()
                .and(eqName(queryOption.name()))
                .and(eqAcademicYear(queryOption.academicYear()))
                .and(eqSemesterType(queryOption.semesterType()))
                .and(eqStudentId(queryOption.studentId()))
                .and(eqNanoId(queryOption.nanoId()))
                .and(eqPaymentKey(queryOption.paymentKey()))
                .and(eqApprovedAt(queryOption.approvedDate()));
    }

    default BooleanExpression eqMember() {
        return order.memberId.eq(member.id);
    }

    default BooleanExpression eqRecruitmentRound() {
        return order.recruitmentRoundId.eq(recruitmentRound.id);
    }

    // TODO: MemberQueryMethod가 interface로 변경된 경우 해당 메서드 제거 및 대체
    default BooleanExpression eqName(String name) {
        return name != null ? member.name.contains(name) : null;
    }

    default BooleanExpression eqAcademicYear(Integer academicYear) {
        return academicYear != null ? recruitmentRound.recruitment.semester.academicYear.eq(academicYear) : null;
    }

    default BooleanExpression eqSemesterType(SemesterType semesterType) {
        return semesterType != null ? recruitmentRound.recruitment.semester.semesterType.eq(semesterType) : null;
    }

    default BooleanExpression eqStudentId(String studentId) {
        return studentId != null ? member.studentId.containsIgnoreCase(studentId) : null;
    }

    default BooleanExpression eqNanoId(String nanoId) {
        return nanoId != null ? order.nanoId.contains(nanoId) : null;
    }

    default BooleanExpression eqPaymentKey(String paymentKey) {
        return paymentKey != null ? order.paymentKey.contains(paymentKey) : null;
    }

    default BooleanExpression eqApprovedAt(LocalDate approvedAt) {
        if (approvedAt == null) {
            return null;
        }
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime startOfDay = approvedAt.atStartOfDay(zoneId);
        ZonedDateTime endOfDay = approvedAt.atTime(LocalTime.MAX).atZone(zoneId);
        return order.approvedAt.between(startOfDay, endOfDay);
    }
}
