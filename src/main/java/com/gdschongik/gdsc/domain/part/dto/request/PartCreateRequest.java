package com.gdschongik.gdsc.domain.part.dto.request;

import com.gdschongik.gdsc.domain.common.vo.Semester;
import com.gdschongik.gdsc.domain.part.domain.PartType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PartCreateRequest(
        @NotNull Semester semester,
        @NotNull PartType partType,
        @NotBlank String thumbnailUrl,
        @NotBlank String leadName,
        @NotNull List<String> introductions,
        @NotNull List<String> recommendations) {}
