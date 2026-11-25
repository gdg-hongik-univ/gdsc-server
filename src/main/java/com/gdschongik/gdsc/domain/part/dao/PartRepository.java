package com.gdschongik.gdsc.domain.part.dao;

import com.gdschongik.gdsc.domain.common.vo.Semester;
import com.gdschongik.gdsc.domain.part.domain.Part;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartRepository extends JpaRepository<Part, Long>, PartCustomRepository {

    List<Part> findAllBySemester(Semester semester);
}
