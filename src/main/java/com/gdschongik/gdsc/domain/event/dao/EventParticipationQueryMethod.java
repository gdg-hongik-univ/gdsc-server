package com.gdschongik.gdsc.domain.event.dao;

import static com.gdschongik.gdsc.domain.event.domain.QEventParticipation.*;
import static com.gdschongik.gdsc.domain.member.domain.QMember.*;

import com.gdschongik.gdsc.domain.event.dto.request.EventParticipantQueryOption;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;

public interface EventParticipationQueryMethod {

    default BooleanBuilder matchesEventParticipantQueryOption(EventParticipantQueryOption queryOption) {
        return new BooleanBuilder()
                .and(eqName(queryOption.name()))
                .and(eqStudentId(queryOption.studentId()))
                .and(eqPhone(queryOption.phone()));
    }

    default BooleanExpression eqMemberId() {
        return eventParticipation.memberId.eq(member.id);
    }

    default BooleanExpression eqEventId(Long eventId) {
        return eventId != null ? eventParticipation.event.id.eq(eventId) : null;
    }

    default BooleanExpression eqName(String name) {
        return name != null ? member.name.contains(name) : null;
    }

    default BooleanExpression eqStudentId(String studentId) {
        return studentId != null ? member.studentId.containsIgnoreCase(studentId) : null;
    }

    default BooleanExpression eqPhone(String phone) {
        return phone != null ? member.phone.containsIgnoreCase(phone) : null;
    }
}
