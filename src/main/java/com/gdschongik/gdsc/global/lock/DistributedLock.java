package com.gdschongik.gdsc.global.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    /**
     * 락 식별자 (SpEL 사용 가능)
     */
    String key();

    /**
     * 락 획득 대기 시간 (초)
     */
    int timeoutSec() default 5;
}
