// src/main/java/com/k2radio/prode/entity/Team.java

package com.k2radio.prode.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 10)
    private String code;

    @Column(length = 255)
    private String flagUrl;

    // ✅ CAMBIAR "group" a "groupName" y mapear a columna "group_name"
    @Column(name = "group_name", length = 10)
    private String groupName;

    @Column(length = 50)
    private String confederation;
}