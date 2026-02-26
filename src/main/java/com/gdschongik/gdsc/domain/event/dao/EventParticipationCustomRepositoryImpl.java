package com.gdschongik.gdsc.domain.event.dao;

import static com.gdschongik.gdsc.domain.event.domain.AfterPartyAttendanceStatus.ATTENDED;
import static com.gdschongik.gdsc.domain.event.domain.PaymentStatus.*;
import static com.gdschongik.gdsc.domain.event.domain.QEventParticipation.*;
import static com.gdschongik.gdsc.domain.member.domain.QMember.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.event.domain.AfterPartyApplicationStatus;
import com.gdschongik.gdsc.domain.event.domain.Event;
import com.gdschongik.gdsc.domain.event.domain.MainEventApplicationStatus;
import com.gdschongik.gdsc.domain.event.dto.dto.AfterPartyApplicantCountDto;
import com.gdschongik.gdsc.domain.event.dto.dto.EventParticipationDto;
import com.gdschongik.gdsc.domain.event.dto.dto.QAfterPartyApplicantCountDto;
import com.gdschongik.gdsc.domain.event.dto.request.EventParticipantQueryOption;
import com.gdschongik.gdsc.domain.event.dto.response.AfterPartyApplicantResponse;
import com.gdschongik.gdsc.domain.event.dto.response.EventApplicantResponse;
import com.gdschongik.gdsc.domain.event.dto.response.QEventApplicantResponse;
import com.gdschongik.gdsc.domain.member.domain.MemberRole;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class EventParticipationCustomRepositoryImpl
        implements EventParticipationCustomRepository, EventParticipationQueryMethod {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<EventApplicantResponse> findEventApplicants(
            Long eventId, EventParticipantQueryOption queryOption, Pageable pageable) {

        OrderSpecifier<?>[] orderSpecifiers = getEventApplicantOrderSpecifiers(pageable);
        List<Long> ids = getIdsByQueryOption(eventId, queryOption, orderSpecifiers);

        List<EventApplicantResponse> fetch = jpaQueryFactory
                .select(new QEventApplicantResponse(eventParticipation, member))
                .from(eventParticipation)
                .innerJoin(member)
                .on(eqMemberId())
                .where(eventParticipation.id.in(ids))
                .orderBy(orderSpecifiers)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return PageableExecutionUtils.getPage(fetch, pageable, ids::size);
    }

    private List<Long> getIdsByQueryOption(
            Long eventId, EventParticipantQueryOption queryOption, @NonNull OrderSpecifier<?>... orderSpecifiers) {
        return jpaQueryFactory
                .select(eventParticipation.id)
                .from(eventParticipation)
                .leftJoin(member)
                .on(eqMemberId())
                .where(eqEventId(eventId), matchesEventParticipantQueryOption(queryOption))
                .orderBy(orderSpecifiers)
                .fetch();
    }

    @Override
    public AfterPartyApplicantResponse findAfterPartyApplicants(
            Long eventId, EventParticipantQueryOption queryOption, Pageable pageable) {

        OrderSpecifier<?>[] orderSpecifiers = getAfterPartyOrderSpecifiers(pageable);
        List<Long> ids = getAfterPartyApplicantIdsByQueryOption(eventId, queryOption, orderSpecifiers);

        List<EventParticipationDto> fetch = jpaQueryFactory
                .selectFrom(eventParticipation)
                .where(eventParticipation.id.in(ids))
                .orderBy(orderSpecifiers)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .stream()
                .map(EventParticipationDto::from)
                .toList();

        AfterPartyApplicantCountDto counts = jpaQueryFactory
                .select(getEventParticipationCounts())
                .from(eventParticipation)
                .where(eqEventId(eventId))
                .fetchOne();

        return AfterPartyApplicantResponse.of(PageableExecutionUtils.getPage(fetch, pageable, ids::size), counts);
    }

    private List<Long> getAfterPartyApplicantIdsByQueryOption(
            Long eventId, EventParticipantQueryOption queryOption, @NonNull OrderSpecifier<?>... orderSpecifiers) {
        return jpaQueryFactory
                .select(eventParticipation.id)
                .from(eventParticipation)
                .where(
                        eqEventId(eventId),
                        eqAfterPartyApplicationStatus(AfterPartyApplicationStatus.APPLIED)
                                .or(eqAfterPartyAttendanceStatus(ATTENDED)),
                        matchesEventParticipantQueryOption(queryOption))
                .orderBy(orderSpecifiers)
                .fetch();
    }

    private QAfterPartyApplicantCountDto getEventParticipationCounts() {
        return new QAfterPartyApplicantCountDto(
                Expressions.cases()
                        .when(eqAfterPartyApplicationStatus(AfterPartyApplicationStatus.APPLIED)
                                .or(eqAfterPartyAttendanceStatus(ATTENDED)))
                        .then(eventParticipation.id)
                        .otherwise((Long) null)
                        .count(),
                Expressions.cases()
                        .when(eqPrePaymentStatus(PAID))
                        .then(eventParticipation.id)
                        .otherwise((Long) null)
                        .count(),
                Expressions.cases()
                        .when(eqAfterPartyAttendanceStatus(ATTENDED))
                        .then(eventParticipation.id)
                        .otherwise((Long) null)
                        .count(),
                Expressions.cases()
                        .when(eqPostPaymentStatus(PAID))
                        .then(eventParticipation.id)
                        .otherwise((Long) null)
                        .count());
    }

    private OrderSpecifier<?>[] getEventApplicantOrderSpecifiers(Pageable pageable) {
        Sort sort = pageable.getSort();

        // 정렬 기준이 없으면 기본값으로 참여자 역할순 -> 최신순 정렬
        if (sort.isUnsorted()) {
            return new OrderSpecifier<?>[] {
                new OrderSpecifier<>(Order.ASC, getParticipantRoleOrderExpression()),
                eventParticipation.createdAt.desc(),
                eventParticipation.id.desc()
            };
        }

        return getDefaultOrderSpecifiers(sort);
    }

    /**
     * 참여자 역할별 정렬을 위한 숫자 표현식을 생성합니다.
     * NON_MEMBER(0) -> GUEST(1) -> ASSOCIATE(2) -> REGULAR(3) 순으로 정렬됩니다.
     */
    private NumberExpression<Integer> getParticipantRoleOrderExpression() {
        return new CaseBuilder()
                .when(member.isNull())
                .then(0)
                .when(member.role.eq(MemberRole.REGULAR))
                .then(3)
                .when(member.role.eq(MemberRole.ASSOCIATE))
                .then(2)
                .otherwise(1);
    }

    private OrderSpecifier<?>[] getAfterPartyOrderSpecifiers(Pageable pageable) {
        Sort sort = pageable.getSort();

        // 정렬 기준이 없으면 기본값으로 최신순 정렬
        if (sort.isUnsorted()) {
            return new OrderSpecifier<?>[] {eventParticipation.createdAt.desc(), eventParticipation.id.desc()};
        }

        return getDefaultOrderSpecifiers(sort);
    }

    private OrderSpecifier<?>[] getDefaultOrderSpecifiers(Sort sort) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();
        for (Sort.Order order : sort) {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            switch (order.getProperty()) {
                case "createdAt" -> {
                    orders.add(new OrderSpecifier<>(direction, eventParticipation.createdAt));
                    orders.add(new OrderSpecifier<>(direction, eventParticipation.id));
                }
                case "name" -> orders.add(new OrderSpecifier<>(direction, eventParticipation.participant.name));
                default -> throw new CustomException(SORT_NOT_SUPPORTED);
            }
        }
        return orders.toArray(OrderSpecifier[]::new);
    }

    @Override
    public long countMainEventApplicantsByEvent(Event event) {
        return Objects.requireNonNull(jpaQueryFactory
                .select(eventParticipation.count())
                .from(eventParticipation)
                .where(eqEventId(event.getId()), eqMainEventApplicationStatus(MainEventApplicationStatus.APPLIED))
                .fetchOne());
    }

    @Override
    public long countAfterPartyApplicantsByEvent(Event event) {
        return Objects.requireNonNull(jpaQueryFactory
                .select(eventParticipation.count())
                .from(eventParticipation)
                .where(eqEventId(event.getId()), eqAfterPartyApplicationStatus(AfterPartyApplicationStatus.APPLIED))
                .fetchOne());
    }
}
