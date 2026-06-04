// src/main/java/com/k2radio/prode/dto/admin/AdminStatsDTO.java

package com.k2radio.prode.dto.admin;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsDTO {
    private Integer totalUsers;
    private Integer totalMatches;
    private Integer finishedMatches;
    private Integer pendingMatches;
}