package com.makersacademy.acebook.repository;

import com.makersacademy.acebook.model.Prompt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PromptRepository extends JpaRepository<Prompt, Long> {

    @Query(value = "SELECT * FROM prompts ORDER BY RANDOM() LIMIT 3", nativeQuery = true)
    List<Prompt> findRandomPrompts();
}