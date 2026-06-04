package com.k2radio.prode.dto.group;

import com.k2radio.prode.dto.admin.GroupStandingDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupDTO {
    private String groupName;
    private List<GroupStandingDTO> standings;
    private Integer matchesPlayed;
    private Integer totalMatches;
    private Boolean completed;
}