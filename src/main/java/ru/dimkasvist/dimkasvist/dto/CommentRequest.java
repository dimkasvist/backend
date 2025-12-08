package ru.dimkasvist.dimkasvist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentRequest {

    @NotBlank(message = "Comment text is required")
    @Size(max = 1000, message = "Comment must be less than 1000 characters")
    private String text;
}
