package com.k2radio.prode.dto.user;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String name;

    @Pattern(regexp = "^\\+54\\d{10}$",
            message = "El teléfono debe estar en formato +54XXXXXXXXXX")
    private String phone;

    private String avatarUrl;
}