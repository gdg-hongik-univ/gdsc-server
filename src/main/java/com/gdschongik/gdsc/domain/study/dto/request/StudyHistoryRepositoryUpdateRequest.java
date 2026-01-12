package com.gdschongik.gdsc.domain.study.dto.request;

import com.gdschongik.gdsc.global.common.constant.RegexConstant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record StudyHistoryRepositoryUpdateRequest(
        @NotNull @Positive Long studyId,
        @NotBlank @Pattern(regexp = RegexConstant.GITHUB_REPOSITORY) String repositoryLink) {}
