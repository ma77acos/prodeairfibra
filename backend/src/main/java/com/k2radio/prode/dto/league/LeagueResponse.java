package com.k2radio.prode.dto.league;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeagueResponse {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Integer maxMembers;
    private Integer memberCount;
    private String ownerName;
    private Long ownerId;
    private LocalDateTime createdAt;
    private Boolean isMember;
    private Boolean isOwner;
}