package com.gdschongik.gdsc.domain.part.api;

import com.gdschongik.gdsc.domain.part.application.PartService;
import com.gdschongik.gdsc.domain.part.dto.request.PartCreateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Part - Admin", description = "파트 어드민 API입니다.")
@RestController
@RequestMapping("/admin/parts")
@RequiredArgsConstructor
public class AdminPartController {

    private final PartService partService;

    @Operation(summary = "파트 개설", description = "파트를 개설합니다.")
    @PostMapping
    public ResponseEntity<Void> createPart(@Valid @RequestBody PartCreateRequest request) {
        partService.createPart(request);
        return ResponseEntity.ok().build();
    }
}
