package com.k2radio.prode.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private Boolean active;
    private Integer totalPoints;
    private Integer exactResults;
    private LocalDateTime createdAt;
}