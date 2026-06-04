package com.k2radio.prode.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupStandingDTO {
    private Integer position;
    private Long teamId;
    private String teamName;
    private String teamCode;
    private String flagUrl;      // unificado con admin
    private String groupName;    // agregado desde admin
    private Integer played;
    private Integer won;
    private Integer drawn;
    private Integer lost;
    private Integer goalsFor;
    private Integer goalsAgainst;
    private Integer goalDiff;  // unificado con admin
    private Integer points;
    private Boolean qualified;   // mantenido de group
}