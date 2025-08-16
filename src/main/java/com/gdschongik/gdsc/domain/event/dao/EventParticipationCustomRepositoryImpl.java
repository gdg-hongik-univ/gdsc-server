package com.gdschongik.gdsc.domain.event.dao;

import static com.gdschongik.gdsc.domain.event.domain.QEventParticipation.*;
import static com.gdschongik.gdsc.domain.member.domain.QMember.*;

import com.gdschongik.gdsc.domain.event.dto.request.EventParticipantQueryOption;
import com.gdschongik.gdsc.domain.event.dto.response.EventApplicantResponse;
import com.gdschongik.gdsc.domain.event.dto.response.QEventApplicantResponse;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.annotation.Nullable;
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
        List<Long> ids = getIdsByQueryOption(eventId, queryOption, null, orderSpecifiers);

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
        Sort.Order order = sort.getOrderFor("createdAt");

        if (sort.isUnsorted() || order == null) {
            return new OrderSpecifier<?>[] {eventParticipation.createdAt.desc(), eventParticipation.id.desc()};
        }

        return order.isAscending()
                ? new OrderSpecifier<?>[] {eventParticipation.createdAt.asc(), eventParticipation.id.asc()}
                : new OrderSpecifier<?>[] {eventParticipation.createdAt.desc(), eventParticipation.id.desc()};
    }

    private List<Long> getIdsByQueryOption(
            Long eventId,
            EventParticipantQueryOption queryOption,
            @Nullable Predicate predicate,
            @NonNull OrderSpecifier<?>... orderSpecifiers) {
        return jpaQueryFactory
                .select(eventParticipation.id)
                .from(eventParticipation)
                .leftJoin(member)
                .on(eventParticipation.memberId.eq(member.id))
                .where(eqEventId(eventId), matchesEventParticipantQueryOption(queryOption), predicate)
                .orderBy(orderSpecifiers)
                .fetch();
    }
}
