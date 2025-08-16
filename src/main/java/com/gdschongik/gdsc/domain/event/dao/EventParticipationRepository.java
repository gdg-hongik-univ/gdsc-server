package com.gdschongik.gdsc.domain.event.dao;

import com.gdschongik.gdsc.domain.event.domain.EventParticipation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventParticipationRepository extends JpaRepository<EventParticipation, Long> {

    List<EventParticipation> findAllByEventId(Long eventId);
}
