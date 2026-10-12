package com.gdschongik.gdsc.domain.statistic.dao;

import com.gdschongik.gdsc.domain.statistic.domain.CommunityStatistic;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityStatisticRepository extends JpaRepository<CommunityStatistic, Long> {

    Optional<CommunityStatistic> findTopByOrderByIdAsc();
}
