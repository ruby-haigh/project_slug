package com.makersacademy.acebook.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "group_cycle_prompts")
public class GroupCyclePrompt {

    // Primary key for this link row
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The cycle this prompt belongs to
    @Column(name = "group_cycle_id", nullable = false)
    private Long groupCycleId;

    // The prompt selected for that cycle
    @Column(name = "prompt_id", nullable = false)
    private Long promptId;

    public GroupCyclePrompt(Long groupCycleId, Long promptId) {
        this.groupCycleId = groupCycleId;
        this.promptId = promptId;
    }
}