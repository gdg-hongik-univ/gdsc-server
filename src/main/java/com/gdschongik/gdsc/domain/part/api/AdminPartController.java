package com.gdschongik.gdsc.domain.part.api;

import com.gdschongik.gdsc.domain.part.application.PartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Part - Admin", description = "파트 어드민 API입니다.")
@RestController
@RequestMapping("/admin/parts")
@RequiredArgsConstructor
public class AdminPartController {

    private final PartService partService;
}
