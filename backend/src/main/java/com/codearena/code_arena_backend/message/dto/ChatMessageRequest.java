package com.codearena.code_arena_backend.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequest {
    @NotNull(message = "Recipient ID must not be null")
    private Long recipientId;

    @NotBlank(message = "Content must not be blank")
    @Size(max = 500, message = "Content size must not exceed 500 characters")
    private String content;
}
