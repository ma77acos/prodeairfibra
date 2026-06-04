// src/main/java/com/k2radio/prode/entity/PrivateLeague.java

package com.k2radio.prode.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "private_leagues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrivateLeague {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 20)
    private String code; // Código para unirse

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(length = 255)
    private String description;

    @Builder.Default
    private Integer maxMembers = 50;

    @Builder.Default
    private Boolean isPublic = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToMany(mappedBy = "leagues")
    @Builder.Default
    private Set<User> members = new HashSet<>();
}