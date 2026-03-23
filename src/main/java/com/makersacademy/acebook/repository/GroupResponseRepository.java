package com.makersacademy.acebook.repository;

import com.makersacademy.acebook.model.GroupCycle;
import com.makersacademy.acebook.model.GroupResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GroupResponseRepository extends JpaRepository<GroupResponse, Long> {

    // Get all response rows for a specific cycle and user
    List<GroupResponse> findByGroupCycleIdAndUserId(Long groupCycleId, Long userId);

    List<GroupResponse> findByGroupCycleId(Long groupCycleId);

    boolean existsByGroupId(Long groupId);

    // Check if a user has already submitted responses in this cycle
    boolean existsByGroupCycleIdAndUserId(Long groupCycleId, Long userId);

    @Query("""
        SELECT gr
        FROM GroupResponse gr
        WHERE gr.groupCycleId = :cycleId
        AND gr.createdAt >= :start
        AND gr.createdAt < :end
    """)
    List<GroupResponse> findResponsesForFirstWeek(
            @Param("cycleId") Long cycleId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
