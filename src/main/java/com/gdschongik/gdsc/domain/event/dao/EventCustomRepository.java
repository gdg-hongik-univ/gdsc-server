package com.gdschongik.gdsc.domain.event.dao;

import com.gdschongik.gdsc.domain.event.dto.response.EventResponse;
import java.util.List;
import org.springframework.data.domain.Sort;

public interface EventCustomRepository {

    List<EventResponse> findAllEvents(Sort sort);
}
