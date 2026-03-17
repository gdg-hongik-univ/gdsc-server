package com.gdschongik.gdsc.domain.event.dao;

import com.querydsl.core.types.dsl.BooleanExpression;

import static com.gdschongik.gdsc.domain.event.domain.QEvent.event;

public interface EventQueryMethod {

    default BooleanExpression containsName(String name) {
        return name != null ? event.name.contains(name) : null;
    }
}
