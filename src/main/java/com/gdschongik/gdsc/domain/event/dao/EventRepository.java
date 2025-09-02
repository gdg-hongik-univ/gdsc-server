package com.gdschongik.gdsc.domain.event.dao;

import com.gdschongik.gdsc.domain.event.domain.Event;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findAllByNameContains(String eventName);
}
