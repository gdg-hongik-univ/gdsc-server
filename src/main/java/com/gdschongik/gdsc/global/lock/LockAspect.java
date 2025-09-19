package com.gdschongik.gdsc.global.lock;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.global.exception.CustomException;
import jakarta.validation.constraints.NotNull;
import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

@Order(1) // 트랜잭션 AOP보다 먼저 실행되어야 합니다.
@Aspect
@Component
@RequiredArgsConstructor
public class LockAspect {

    private final LockUtil lockUtil;
    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(com.gdschongik.gdsc.global.lock.DistributedLock)")
    public Object around(@NotNull ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

        String key = parseLockKey(distributedLock, method, joinPoint.getArgs(), joinPoint.getTarget());
        boolean lockAcquired = false;

        try {
            lockAcquired = lockUtil.acquireLock(key, distributedLock.timeoutSec());

            if (!lockAcquired) {
                throw new CustomException(LOCK_ACQUIRE_FAILED);
            }

            return joinPoint.proceed();
        } finally {
            if (lockAcquired) {
                lockUtil.releaseLock(key);
            }
        }
    }

    /**
     * SpEL을 사용하여 키를 파싱합니다.
     */
    private String parseLockKey(DistributedLock distributedLock, Method method, Object[] args, Object target) {
        String key = distributedLock.key();

        // 어노테이션에 락 이름이 비어있으면 클래스명:메서드명으로 기본 값 생성
        if (key.isEmpty()) {
            return String.format("%s:%s", target.getClass().getSimpleName(), method.getName());
        }

        // SpEL 표현식 평가
        MethodBasedEvaluationContext context =
                new MethodBasedEvaluationContext(target, method, args, parameterNameDiscoverer);

        return parser.parseExpression(key).getValue(context, String.class);
    }
}
