package com.traffic.repository;

import com.traffic.domain.CommandExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommandExecutionRepository extends JpaRepository<CommandExecution, Long> {

    Page<CommandExecution> findByControllerIdOrderByExecutedAtDesc(String controllerId, Pageable pageable);
}
