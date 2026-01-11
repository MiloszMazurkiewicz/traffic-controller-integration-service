package com.traffic.repository;

import com.traffic.domain.ControllerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ControllerStatusRepository extends JpaRepository<ControllerStatus, Long> {

    Optional<ControllerStatus> findTopByControllerIdOrderByFetchedAtDesc(String controllerId);
}
