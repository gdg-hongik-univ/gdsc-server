package com.gdschongik.gdsc.domain.part.api;

import com.gdschongik.gdsc.domain.common.model.SemesterType;
import com.gdschongik.gdsc.domain.part.application.PartService;
import com.gdschongik.gdsc.domain.part.dto.response.PartResponse;
import com.gdschongik.gdsc.domain.part.dto.response.PartSemesterResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Part - Common", description = "공통 파트 API입니다.")
@RestController
@RequestMapping("/common/parts")
@RequiredArgsConstructor
public class CommonPartController {

    private final PartService partService;

    @Operation(summary = "파트 개설 학기 목록 조회", description = "파트가 개설된 학년도와 학기 조합을 중복 없이 조회합니다.")
    @GetMapping("/semesters")
    public ResponseEntity<List<PartSemesterResponse>> getAllPartSemesters() {
        var response = partService.getAllPartSemesters();
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "학기별 파트 목록 조회", description = "학년도와 학기가 일치하는 모든 파트를 조회합니다.")
    @GetMapping
    public ResponseEntity<List<PartResponse>> getAllPartsBySemester(
            @RequestParam(name = "academicYear") Integer academicYear,
            @RequestParam(name = "semesterType") SemesterType semesterType) {
        var response = partService.getAllPartsBySemester(academicYear, semesterType);
        return ResponseEntity.ok().body(response);
    }
}
