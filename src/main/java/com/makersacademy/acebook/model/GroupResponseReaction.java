package com.makersacademy.acebook.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "group_response_reactions")
public class GroupResponseReaction {

    // one row = one user's reaction on one response
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // which response this reaction belongs to
    @Column(name = "group_response_id", nullable = false)
    private Long groupResponseId;

    // which user made the reaction
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // chosen emoji
    @Column(name = "emoji", nullable = false)
    private String emoji;

    public GroupResponseReaction(Long groupResponseId, Long userId, String emoji) {
        this.groupResponseId = groupResponseId;
        this.userId = userId;
        this.emoji = emoji;
    }
}