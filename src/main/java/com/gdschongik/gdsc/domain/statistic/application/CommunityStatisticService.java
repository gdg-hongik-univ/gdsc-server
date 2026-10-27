package com.gdschongik.gdsc.domain.statistic.application;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.member.dao.MemberRepository;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.member.domain.service.MemberValidator;
import com.gdschongik.gdsc.domain.statistic.dao.CommunityStatisticRepository;
import com.gdschongik.gdsc.domain.statistic.domain.CommunityStatistic;
import com.gdschongik.gdsc.domain.statistic.dto.response.CommunityStatisticResponse;
import com.gdschongik.gdsc.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityStatisticService {

    private final CommunityStatisticRepository communityStatisticRepository;
    private final MemberRepository memberRepository;
    private final MemberValidator memberValidator;

    // todo: 세미나 도메인 생기면 updateCommunityStatistic로 통합
    @Transactional
    public void updateTotalSeminarCount(String currentMemberDiscordUsername, Integer totalSeminarCount) {
        Member currentMember = memberRepository
                .findByDiscordUsername(currentMemberDiscordUsername)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
        memberValidator.validateAdminPermission(currentMember.getManageRole());

        CommunityStatistic communityStatistic = communityStatisticRepository
                .findTopByOrderByIdAsc()
                .orElseThrow(() -> new CustomException(COMMUNITY_STATISTIC_NOT_FOUND));

        communityStatistic.updateTotalSeminarCount(totalSeminarCount);
        communityStatisticRepository.save(communityStatistic);

        log.info(
                "[CommunityStatisticService] 커뮤니티 통계 누적 세미나 수 갱신: memberId={}, totalSeminarCount={}",
                currentMember.getId(),
                totalSeminarCount);
    }

    public CommunityStatisticResponse getCommunityStatistic() {
        CommunityStatistic communityStatistic = communityStatisticRepository
                .findTopByOrderByIdAsc()
                .orElseThrow(() -> new CustomException(COMMUNITY_STATISTIC_NOT_FOUND));

        return CommunityStatisticResponse.from(communityStatistic);
    }

    // todo: 프로젝트 도메인 생성 후 프로젝트 수 집계 로직 추가
    @Transactional
    public void updateCommunityStatistic() {
        int totalMemberCount = (int) memberRepository.count();

        CommunityStatistic communityStatistic = communityStatisticRepository
                .findTopByOrderByIdAsc()
                .orElseThrow(() -> new CustomException(COMMUNITY_STATISTIC_NOT_FOUND));

        communityStatistic.updateTotalMemberCount(totalMemberCount);
        communityStatisticRepository.save(communityStatistic);

        log.info("[CommunityStatisticService] 커뮤니티 통계 최신화: totalMemberCount={}", totalMemberCount);
    }
}
