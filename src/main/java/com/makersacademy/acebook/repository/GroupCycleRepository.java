package com.makersacademy.acebook.repository;

import com.makersacademy.acebook.model.GroupCycle;
import com.makersacademy.acebook.model.GroupResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface GroupCycleRepository extends JpaRepository<GroupCycle, Long> {

    // Find the current active cycle for a given group
    @Query("""
        SELECT gc
        FROM GroupCycle gc
        WHERE gc.groupId = :groupId
        AND :now BETWEEN gc.cycleStart AND gc.cycleEnd
        """)
    Optional<GroupCycle> findCurrentCycleByGroupId(Long groupId, LocalDateTime now);

    Optional<GroupCycle> findTopByGroupIdOrderByCycleStartDesc(Long groupId);

}



