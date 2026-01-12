package com.gdschongik.gdsc.domain.study.api;

import com.gdschongik.gdsc.domain.study.application.StudentAttendanceService;
import com.gdschongik.gdsc.domain.study.dto.request.AttendanceCreateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Study Attendance - Student", description = "사용자 스터디 출석체크 API입니다.")
@RestController
@RequestMapping("/v2/attendances")
@RequiredArgsConstructor
public class StudentAttendanceController {

    private final StudentAttendanceService studentAttendanceService;

    @Operation(summary = "스터디 출석체크", description = "스터디에 출석체크합니다.")
    @PostMapping("/attend")
    public ResponseEntity<Void> attend(
            @RequestParam(name = "studySessionId") Long studySessionId,
            @Valid @RequestBody AttendanceCreateRequest request) {
        studentAttendanceService.attend(studySessionId, request);
        return ResponseEntity.ok().build();
    }
}
