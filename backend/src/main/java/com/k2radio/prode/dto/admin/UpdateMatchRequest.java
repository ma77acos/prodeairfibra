package com.k2radio.prode.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMatchRequest {
    @NotNull
    private LocalDateTime matchDateTime;

    private String stadium;
    private String city;
    private String country;
}