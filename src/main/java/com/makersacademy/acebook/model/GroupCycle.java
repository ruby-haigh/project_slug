package com.makersacademy.acebook.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@Table(name = "group_cycles")
public class GroupCycle {

    // Primary key for each cycle row
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The group this cycle belongs to
    @Column(name = "group_id", nullable = false)
    private Long groupId;

    // When this cycle starts
    @Column(name = "cycle_start", nullable = false)
    private LocalDateTime cycleStart;

    // When this cycle ends
    @Column(name = "cycle_end", nullable = false)
    private LocalDateTime cycleEnd;

    // When the cycle row was created
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public GroupCycle(Long groupId, LocalDateTime cycleStart, LocalDateTime cycleEnd) {
        this.groupId = groupId;
        this.cycleStart = cycleStart;
        this.cycleEnd = cycleEnd;
    }
}