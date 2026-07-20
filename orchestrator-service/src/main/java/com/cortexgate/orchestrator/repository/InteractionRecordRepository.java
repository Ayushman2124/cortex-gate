package com.cortexgate.orchestrator.repository;

import com.cortexgate.orchestrator.entity.InteractionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InteractionRecordRepository extends JpaRepository<InteractionRecord, Long> {
}
