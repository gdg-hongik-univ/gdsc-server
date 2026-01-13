package com.gdschongik.gdsc.domain.study.dao;

import com.gdschongik.gdsc.domain.common.vo.Semester;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.study.domain.Study;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyRepository extends JpaRepository<Study, Long>, StudyCustomRepository {

    List<Study> findAllByMentor(Member mentor);

    Optional<Study> findByTitleAndSemester(String title, Semester semester);
}
