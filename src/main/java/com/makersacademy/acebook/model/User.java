package com.makersacademy.acebook.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

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

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

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
                && dateOfBirth != null
                && phoneNumber != null && !phoneNumber.isBlank();
    }

    public String getAvatarColour() {
        String[] pastels = {"f2c4ce", "c4def2", "c4f2d5", "f2e8c4", "e8c4f2", "f2cfc4"};
        return pastels[(int)(Math.abs(email.hashCode()) % pastels.length)];
    }

    public String getWhatsAppNumber() {
        if (phoneNumber == null || phoneNumber.isBlank()) return null;

        String cleaned = phoneNumber.replaceAll("[\\s\\-\\(\\)]", "");

        if (cleaned.startsWith("+")) {
            cleaned = cleaned.substring(1);
        }

        if (cleaned.startsWith("07")) {
            cleaned = "44" + cleaned.substring(1);
        }

        System.out.println("Cleaned number is" + cleaned);
        return cleaned;
    }
}
