package com.traffic.repository;

import com.traffic.domain.DetectorReading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface DetectorReadingRepository extends JpaRepository<DetectorReading, Long> {

    @Query("SELECT dr FROM DetectorReading dr WHERE dr.controllerId = :controllerId " +
           "AND dr.fetchedAt = (SELECT MAX(dr2.fetchedAt) FROM DetectorReading dr2 WHERE dr2.controllerId = :controllerId)")
    List<DetectorReading> findLatestByControllerId(@Param("controllerId") String controllerId);

    Page<DetectorReading> findByControllerIdAndFetchedAtBetween(
            String controllerId,
            Instant from,
            Instant to,
            Pageable pageable);

    Page<DetectorReading> findByControllerId(String controllerId, Pageable pageable);
}
