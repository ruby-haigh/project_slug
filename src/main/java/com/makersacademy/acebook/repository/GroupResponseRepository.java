package com.makersacademy.acebook.repository;

import com.makersacademy.acebook.model.GroupResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupResponseRepository extends JpaRepository<GroupResponse, Long> {

    // Get all response rows for a specific cycle and user
    List<GroupResponse> findByGroupCycleIdAndUserId(Long groupCycleId, Long userId);
    List<GroupResponse> findByGroupCycleId(Long groupCycleId);
    boolean existsByGroupId(Long groupId);
    // Check if a user has already submitted responses in this cycle
    boolean existsByGroupCycleIdAndUserId(Long groupCycleId, Long userId);
}
