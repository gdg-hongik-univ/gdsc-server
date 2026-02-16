package com.gdschongik.gdsc.domain.auth.application;

import static com.gdschongik.gdsc.global.common.constant.SecurityConstant.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.auth.dao.RefreshTokenRepository;
import com.gdschongik.gdsc.domain.auth.domain.RefreshToken;
import com.gdschongik.gdsc.domain.auth.dto.AccessTokenDto;
import com.gdschongik.gdsc.domain.auth.dto.RefreshTokenDto;
import com.gdschongik.gdsc.domain.auth.dto.TokenPairDto;
import com.gdschongik.gdsc.domain.member.dao.MemberRepository;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.member.domain.MemberManageRole;
import com.gdschongik.gdsc.domain.member.domain.MemberRole;
import com.gdschongik.gdsc.domain.member.domain.MemberStudyRole;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.security.MemberAuthInfo;
import com.gdschongik.gdsc.global.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRepository memberRepository;

    public AccessTokenDto createAccessToken(MemberAuthInfo authInfo) {
        return jwtUtil.generateAccessToken(authInfo);
    }

    public RefreshTokenDto createRefreshToken(Long memberId) {
        RefreshTokenDto refreshTokenDto = jwtUtil.generateRefreshToken(memberId);
        saveRefreshTokenToRedis(refreshTokenDto);
        return refreshTokenDto;
    }

    private void saveRefreshTokenToRedis(RefreshTokenDto refreshTokenDto) {
        RefreshToken refreshToken =
                RefreshToken.create(refreshTokenDto.memberId(), refreshTokenDto.tokenValue(), refreshTokenDto.ttl());

        refreshTokenRepository.save(refreshToken);
    }

    public TokenPairDto issueTokenPair(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
        MemberAuthInfo authInfo = MemberAuthInfo.from(member);
        AccessTokenDto accessTokenDto = createAccessToken(authInfo);
        RefreshTokenDto refreshTokenDto = createRefreshToken(memberId);
        return new TokenPairDto(accessTokenDto.tokenValue(), refreshTokenDto.tokenValue());
    }

    public AccessTokenDto retrieveAccessToken(String accessTokenValue) {
        try {
            return jwtUtil.parseAccessToken(accessTokenValue);
        } catch (ExpiredJwtException e) {
            return null;
        }
    }

    public RefreshTokenDto retrieveRefreshToken(String refreshTokenValue) {
        RefreshTokenDto refreshTokenDto = parseRefreshToken(refreshTokenValue);

        if (refreshTokenDto == null) {
            return null;
        }

        // 파싱된 DTO와 일치하는 토큰이 Redis에 저장되어 있는지 확인
        Optional<RefreshToken> refreshToken = getRefreshTokenFromRedis(refreshTokenDto.memberId());

        // Redis에 토큰이 존재하고, 쿠키의 토큰과 값이 일치하면 DTO 반환
        if (refreshToken.isPresent()
                && refreshTokenDto.tokenValue().equals(refreshToken.get().getToken())) {
            return refreshTokenDto;
        }

        // Redis에 토큰이 존재하지 않거나, 쿠키의 토큰과 값이 일치하지 않으면 null 반환
        return null;
    }

    private Optional<RefreshToken> getRefreshTokenFromRedis(Long memberId) {
        // TODO: CustomException으로 바꾸기
        return refreshTokenRepository.findById(memberId);
    }

    private RefreshTokenDto parseRefreshToken(String refreshTokenValue) {
        try {
            return jwtUtil.parseRefreshToken(refreshTokenValue);
        } catch (Exception e) {
            return null;
        }
    }

    public AccessTokenDto reissueAccessTokenIfExpired(String accessTokenValue) {
        // AT가 만료된 경우 AT를 재발급, 만료되지 않은 경우 null 반환
        try {
            jwtUtil.parseAccessToken(accessTokenValue);
            return null;
        } catch (ExpiredJwtException e) {
            Claims claims = e.getClaims();

            Long memberId = Long.parseLong(claims.getSubject());
            MemberRole memberRole = MemberRole.valueOf(claims.get(TOKEN_ROLE_NAME, String.class));
            MemberManageRole memberManageRole =
                    MemberManageRole.valueOf(claims.get(TOKEN_MANAGE_ROLE_NAME, String.class));
            MemberStudyRole memberStudyRole = MemberStudyRole.valueOf(claims.get(TOKEN_STUDY_ROLE_NAME, String.class));
            var authInfo = new MemberAuthInfo(memberId, memberRole, memberManageRole, memberStudyRole);

            return createAccessToken(authInfo);
        }
    }
}
