package com.gdschongik.gdsc.domain.email.dto.request;

/**
 * @deprecated 링크 클릭 방식의 JWT payload. 코드 인증 방식에서는 사용하지 않는다.
 */
@Deprecated
public record UnivEmailVerificationTokenDto(Long memberId, String email) {}
