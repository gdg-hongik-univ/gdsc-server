package com.gdschongik.gdsc.domain.event.dao;

import static com.gdschongik.gdsc.domain.event.domain.AfterPartyApplicationStatus.APPLIED;
import static com.gdschongik.gdsc.domain.event.domain.AfterPartyAttendanceStatus.ATTENDED;
import static com.gdschongik.gdsc.domain.event.domain.PaymentStatus.*;
import static com.gdschongik.gdsc.domain.event.domain.QEventParticipation.*;
import static com.gdschongik.gdsc.domain.member.domain.QMember.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.event.dto.dto.AfterPartyApplicantCountDto;
import com.gdschongik.gdsc.domain.event.dto.dto.EventParticipationDto;
import com.gdschongik.gdsc.domain.event.dto.dto.QAfterPartyApplicantCountDto;
import com.gdschongik.gdsc.domain.event.dto.request.EventParticipantQueryOption;
import com.gdschongik.gdsc.domain.event.dto.response.AfterPartyApplicantResponse;
import com.gdschongik.gdsc.domain.event.dto.response.EventApplicantResponse;
import com.gdschongik.gdsc.domain.event.dto.response.QEventApplicantResponse;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
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

        OrderSpecifier<?>[] orderSpecifiers = getOrderSpecifiers(pageable);
        List<Long> ids = getIdsByQueryOption(eventId, queryOption, orderSpecifiers);

        List<EventApplicantResponse> fetch = jpaQueryFactory
                .select(new QEventApplicantResponse(eventParticipation, member))
                .from(eventParticipation)
                .leftJoin(member)
                .on(eqMemberId())
                .where(eventParticipation.id.in(ids))
                .orderBy(orderSpecifiers)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return PageableExecutionUtils.getPage(fetch, pageable, ids::size);
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(Pageable pageable) {
        Sort sort = pageable.getSort();

        // 정렬 기준이 없으면 기본값으로 최신순 정렬
        if (sort.isUnsorted()) {
            return new OrderSpecifier<?>[] {eventParticipation.createdAt.desc(), eventParticipation.id.desc()};
        }

        // 정렬 기준에 따라 OrderSpecifier 생성
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

        OrderSpecifier<?>[] orderSpecifiers = getOrderSpecifiers(pageable);
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
                        eqAfterPartyApplicationStatus(APPLIED).or(eqAfterPartyAttendanceStatus(ATTENDED)),
                        matchesEventParticipantQueryOption(queryOption))
                .orderBy(orderSpecifiers)
                .fetch();
    }

    private QAfterPartyApplicantCountDto getEventParticipationCounts() {
        return new QAfterPartyApplicantCountDto(
                Expressions.cases()
                        .when(eqAfterPartyApplicationStatus(APPLIED).or(eqAfterPartyAttendanceStatus(ATTENDED)))
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
}
