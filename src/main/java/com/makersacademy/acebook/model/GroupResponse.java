package com.makersacademy.acebook.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// stores each submitted answer
// one row = one user + one prompt + one cycle

@Data
@Entity
@NoArgsConstructor
@Table(name = "group_responses")
public class GroupResponse {

    // Primary key for each response row
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The cycle this response belongs to
    @Column(name = "group_cycle_id", nullable = false)
    private Long groupCycleId;

    // The group this response belongs to
    @Column(name = "group_id", nullable = false)
    private Long groupId;

    // The user who submitted this response
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // The prompt this answer is for
    @Column(name = "prompt_id", nullable = false)
    private Long promptId;

    // The actual written response
    @Column(name = "response_text", nullable = false)
    private String responseText;

    // When the response row was created
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "image_url")
    private String imageUrl;

    public GroupResponse(Long groupCycleId, Long groupId, Long userId, Long promptId, String responseText) {
        this.groupCycleId = groupCycleId;
        this.groupId = groupId;
        this.userId = userId;
        this.promptId = promptId;
        this.responseText = responseText;
    }
}