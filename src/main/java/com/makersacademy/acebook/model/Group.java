package com.makersacademy.acebook.model;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

    @OneToMany(mappedBy = "group")
    private List<GroupMembership> members;

    public Group(String name) {
        this.name = name;
        this.frequency = "MONTHLY";
    }

}
