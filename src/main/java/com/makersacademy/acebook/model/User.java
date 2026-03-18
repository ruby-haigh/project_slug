package com.makersacademy.acebook.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static java.lang.Boolean.TRUE;

@Data
@Entity
@Getter @Setter @NoArgsConstructor
@Table(name = "USERS")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String name;
    private String bio;

    public User(String email) {
        this.email = email;
    }

    public User(String email, String name, String bio) {
        this.email = email;
        this.name = name;
        this.bio = bio;
    }
}
