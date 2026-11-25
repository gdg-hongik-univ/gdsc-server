package com.gdschongik.gdsc.domain.part.dao;

import com.gdschongik.gdsc.domain.part.dto.response.PartSemesterResponse;
import java.util.List;

public interface PartCustomRepository {

    List<PartSemesterResponse> findAllPartSemesters();
}
