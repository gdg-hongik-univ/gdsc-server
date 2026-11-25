package com.gdschongik.gdsc.domain.part.dto.response;

import com.gdschongik.gdsc.domain.part.domain.Part;
import com.gdschongik.gdsc.domain.part.domain.PartSession;
import com.gdschongik.gdsc.domain.part.domain.PartType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record PartResponse(
        Long partId,
        PartType partType,
        String thumbnailUrl,
        @Schema(description = "파트 리드 이름") String leadName,
        @Schema(description = "파트 소개 목록") List<String> introductions,
        @Schema(description = "추천 대상 목록") List<String> recommendations,
        @Schema(description = "파트 세션 목록") List<Item> partSessions) {

    public static PartResponse from(Part part) {
        return new PartResponse(
                part.getId(),
                part.getPartType(),
                part.getThumbnailUrl(),
                part.getLeadName(),
                part.getIntroductions().stream().toList(),
                part.getRecommendations().stream().toList(),
                part.getPartSessions().stream().map(Item::from).toList());
    }

    public record Item(
            @Schema(description = "세션 순서") Integer position,
            @Schema(description = "세션 제목") String title,
            @Schema(description = "세션 설명") String description,
            @Schema(description = "세션 강조 여부") boolean isHighlighted) {

        private static Item from(PartSession session) {
            return new Item(
                    session.getPosition(), session.getTitle(), session.getDescription(), session.isHighlighted());
        }
    }
}
