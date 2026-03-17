package com.gdschongik.gdsc.domain.event.dao;

import static com.gdschongik.gdsc.domain.event.domain.QEvent.event;

import com.gdschongik.gdsc.domain.event.domain.Event;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class EventCustomRepositoryImpl implements EventCustomRepository, EventQueryMethod {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Event> findAllByNameContains(String name, Pageable pageable) {
        List<Long> ids = getEventIdsContainsName(name);

        List<Event> fetch = jpaQueryFactory
                .selectFrom(event)
                .where(event.id.in(ids))
                .fetch();

        return PageableExecutionUtils.getPage(fetch, pageable, ids::size);
    }

    private List<Long> getEventIdsContainsName(String name) {
        return jpaQueryFactory
                .select(event.id)
                .from(event)
                .where(containsName(name))
                .fetch();
    }
}
