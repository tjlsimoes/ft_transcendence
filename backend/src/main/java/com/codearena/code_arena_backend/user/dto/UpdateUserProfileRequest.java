package com.codearena.code_arena_backend.user.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserProfileRequest {

    @Size(max = 100, message = "displayName must be at most 100 characters")
    private String displayName;

    @Size(max = 2000, message = "bio must be at most 2000 characters")
    private String bio;
}
