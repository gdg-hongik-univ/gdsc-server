package com.gdschongik.gdsc.domain.event.dao;

import static com.gdschongik.gdsc.domain.event.domain.QEventParticipation.*;
import static com.gdschongik.gdsc.domain.member.domain.QMember.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.event.dto.request.EventParticipantQueryOption;
import com.gdschongik.gdsc.domain.event.dto.response.EventApplicantResponse;
import com.gdschongik.gdsc.domain.event.dto.response.QEventApplicantResponse;
import com.gdschongik.gdsc.global.exception.CustomException;
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

        // 정렬 기준이 없으면 기본값으로 최신순 정렬
        if (sort.isUnsorted()) {
            return new OrderSpecifier<?>[] {eventParticipation.createdAt.desc(), eventParticipation.id.desc()};
        }

        Sort.Order order = sort.getOrderFor("createdAt");

        // createdAt에 대한 정렬만 허용
        if (order == null) {
            throw new CustomException(SORT_NOT_SUPPORTED);
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
                .on(eqMemberId())
                .where(eqEventId(eventId), matchesEventParticipantQueryOption(queryOption), predicate)
                .orderBy(orderSpecifiers)
                .fetch();
    }
}
