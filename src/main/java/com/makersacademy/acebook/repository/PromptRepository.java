package com.makersacademy.acebook.repository;

import com.makersacademy.acebook.model.Prompt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

// used to fetch random prompts when a new cycle starts

public interface PromptRepository extends JpaRepository<Prompt, Long> {

    @Query(value = "SELECT * FROM prompts ORDER BY RANDOM() LIMIT 4", nativeQuery = true)
    List<Prompt> findRandomPrompts();

    @Query(value = """
            SELECT *
            FROM prompts
            WHERE LOWER(prompt) LIKE '%spotify%'
               OR LOWER(prompt) LIKE '%song%'
               OR LOWER(prompt) LIKE '%track%'
               OR LOWER(prompt) LIKE '%playlist%'
            ORDER BY RANDOM()
            LIMIT 1
            """, nativeQuery = true)
    List<Prompt> findRandomSongPrompt();

    @Query(value = """
            SELECT *
            FROM prompts
            WHERE id <> :excludedPromptId
              AND LOWER(prompt) NOT LIKE '%spotify%'
              AND LOWER(prompt) NOT LIKE '%song%'
              AND LOWER(prompt) NOT LIKE '%track%'
              AND LOWER(prompt) NOT LIKE '%playlist%'
            ORDER BY RANDOM()
            LIMIT :limit
            """, nativeQuery = true)
    List<Prompt> findRandomNonSongPromptsExcluding(Long excludedPromptId, int limit);
}
