package com.gdschongik.gdsc.domain.event.dao;

import static com.gdschongik.gdsc.domain.event.domain.QEvent.*;
import static com.gdschongik.gdsc.domain.event.domain.QEventParticipation.*;

import com.gdschongik.gdsc.domain.event.dto.response.EventResponse;
import com.gdschongik.gdsc.domain.event.dto.response.QEventResponse;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

@RequiredArgsConstructor
public class EventCustomRepositoryImpl implements EventCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<EventResponse> findAllEvents(Sort sort) {
        return jpaQueryFactory
                .select(new QEventResponse(event, eventParticipation.count()))
                .from(event)
                .leftJoin(eventParticipation)
                .on(event.id.eq(eventParticipation.event.id))
                .groupBy(event.id)
                .orderBy(getOrderSpecifiers(sort))
                .fetch();
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort) {
        Sort.Order order = sort.getOrderFor("name");

        if (order != null) {
            return order.isAscending()
                    ? new OrderSpecifier<?>[] {event.name.asc(), event.id.asc()}
                    : new OrderSpecifier<?>[] {event.name.desc(), event.id.desc()};
        }

        // 정렬 기준이 없으면 기본값으로 최신순 정렬
        return new OrderSpecifier<?>[] {event.createdAt.desc(), event.id.desc()};
    }
}
