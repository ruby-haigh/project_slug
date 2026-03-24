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
    private Integer age;
    @Column(name = "phone_number")
    private String phoneNumber;
    private String bio;
    private String profilePictureUrl;

    public User(String email, String name, String bio) {
        this.email = email;
        this.name = name;
        this.bio = bio;
    }

    public boolean isProfileComplete() {
        return name != null && !name.isBlank()
                && age != null
                && phoneNumber != null && !phoneNumber.isBlank();
    }

    public String getAvatarColour() {
        String[] pastels = {"f2c4ce", "c4def2", "c4f2d5", "f2e8c4", "e8c4f2", "f2cfc4"};
        return pastels[(int)(Math.abs(email.hashCode()) % pastels.length)];
    }
}
