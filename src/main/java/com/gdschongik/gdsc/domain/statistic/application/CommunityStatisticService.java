package com.gdschongik.gdsc.domain.statistic.application;

import com.gdschongik.gdsc.domain.statistic.dao.CommunityStatisticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityStatisticService {
    private final CommunityStatisticRepository communityStatisticRepository;
}
