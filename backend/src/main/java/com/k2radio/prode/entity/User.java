// src/main/java/com/k2radio/prode/entity/User.java

package com.k2radio.prode.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, unique = false, length = 15)
    private String phone;

    @Column(unique = true, length = 8)
    private String dni;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false)
    private String password;

    @Column(length = 255)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    @Builder.Default
    private Boolean active = true;

    @Builder.Default
    private Boolean emailVerified = false;

    private String verificationToken;

    private String resetPasswordToken;

    private LocalDateTime resetPasswordExpiry;

    @Column(length = 50)
    private String provider; // LOCAL, GOOGLE

    private String providerId;

    @Builder.Default
    private Integer totalPoints = 0;

    @Builder.Default
    private Integer exactResults = 0;

    @Builder.Default
    private Integer correctWinners = 0;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Prediction> predictions = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "user_leagues",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "league_id")
    )
    @Builder.Default
    private Set<PrivateLeague> leagues = new HashSet<>();

    public enum Role {
        USER, ADMIN
    }
}