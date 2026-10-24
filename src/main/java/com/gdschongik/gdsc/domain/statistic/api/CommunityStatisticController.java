package com.gdschongik.gdsc.domain.statistic.api;

import com.gdschongik.gdsc.domain.statistic.application.CommunityStatisticService;
import com.gdschongik.gdsc.domain.statistic.dto.response.CommunityStatisticResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Community Statistic", description = "커뮤니티 통계 API입니다.")
@RestController
@RequestMapping("/community-statistics")
@RequiredArgsConstructor
public class CommunityStatisticController {

    private final CommunityStatisticService communityStatisticService;

    @GetMapping
    public ResponseEntity<CommunityStatisticResponse> getCommunityStatistic() {
        var response = communityStatisticService.getCommunityStatistic();
        return ResponseEntity.ok(response);
    }
}
