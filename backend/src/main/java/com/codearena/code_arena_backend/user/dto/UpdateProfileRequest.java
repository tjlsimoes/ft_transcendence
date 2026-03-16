package com.codearena.code_arena_backend.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {

    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    private String username;

    @Email(message = "Must be a valid email address")
    private String email;

    @Size(max = 255, message = "Avatar URL must have at most 255 characters")
    private String avatar;
}
