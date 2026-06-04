package com.k2radio.prode.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTeamRequest {

    @NotBlank(message = "El nombre es requerido")
    private String name;

    @NotBlank(message = "El código es requerido")
    @Size(min = 3, max = 3, message = "El código debe tener 3 caracteres")
    private String code;

    private String flagUrl;
    private String groupName;
}