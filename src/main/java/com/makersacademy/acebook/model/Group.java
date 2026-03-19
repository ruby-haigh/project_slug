package com.makersacademy.acebook.model;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Entity
@NoArgsConstructor
@Table(name = "GROUPS")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String frequency = "MONTHLY";

    @OneToMany
    @JoinColumn(name="group_id")
    private List<GroupMembership> memberships;
    public Group(String name) {
        this.name = name;
        this.frequency = "MONTHLY";
    }

    public List<User> getMembers() {
        // Go through each membership and get the user
        if (memberships == null) return Collections.emptyList();
        return memberships.stream()
                .map(GroupMembership::getUser)
                .collect(Collectors.toList());
    }

}
