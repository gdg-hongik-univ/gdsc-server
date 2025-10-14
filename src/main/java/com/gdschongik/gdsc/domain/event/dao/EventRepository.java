package com.gdschongik.gdsc.domain.event.dao;

import com.gdschongik.gdsc.domain.event.domain.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findAllByNameContains(String name, Pageable pageable);
}
