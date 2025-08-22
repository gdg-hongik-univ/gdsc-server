package com.gdschongik.gdsc.domain.event.dao;

import com.gdschongik.gdsc.domain.event.domain.Event;
import com.gdschongik.gdsc.domain.event.domain.EventParticipation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventParticipationRepository
        extends JpaRepository<EventParticipation, Long>, EventParticipationCustomRepository {
    List<EventParticipation> findAllByEventAndMemberIdIn(Event event, List<Long> memberIds);

    List<EventParticipation> findAllByEvent(Event event);
}
