package com.gdschongik.gdsc.domain.study.dao;

import com.gdschongik.gdsc.domain.study.domain.Study;
import java.util.List;
import java.util.Optional;

public interface StudyCustomRepository {
    Optional<Study> findFetchById(Long id);

    Optional<Study> findFetchBySessionId(Long sessionId);

    List<Study> findFetchAll();
}
