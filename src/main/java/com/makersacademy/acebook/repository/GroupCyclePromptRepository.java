package com.makersacademy.acebook.repository;

import com.makersacademy.acebook.model.GroupCyclePrompt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupCyclePromptRepository extends JpaRepository<GroupCyclePrompt, Long> {

    // Get all prompt-link rows for a specific cycle
    List<GroupCyclePrompt> findByGroupCycleId(Long groupCycleId);
}