package com.gdschongik.gdsc.domain.event.dao;

import com.gdschongik.gdsc.domain.event.domain.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventCustomRepository {

    Page<Event> findAllByNameContains(String name, Pageable pageable);
}
