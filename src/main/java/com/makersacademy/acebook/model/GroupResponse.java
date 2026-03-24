package com.makersacademy.acebook.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Entity
@NoArgsConstructor
@Table(name = "group_responses")
public class GroupResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_cycle_id", nullable = false)
    private Long groupCycleId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "prompt_id", nullable = false)
    private Long promptId;

    @Column(name = "response_text", nullable = true)
    private String responseText;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "spotify_track_url")
    private String spotifyTrackUrl;

    public GroupResponse(Long groupCycleId, Long groupId, Long userId, Long promptId, String responseText) {
        this.groupCycleId = groupCycleId;
        this.groupId = groupId;
        this.userId = userId;
        this.promptId = promptId;
        this.responseText = responseText;
    }

    public GroupResponse(Long groupCycleId, Long groupId, Long userId, Long promptId, String responseText, LocalDateTime createdAt) {
        this.groupCycleId = groupCycleId;
        this.groupId = groupId;
        this.userId = userId;
        this.promptId = promptId;
        this.responseText = responseText;
        this.createdAt = createdAt;
    }
}
