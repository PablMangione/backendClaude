package com.acainfo.backendclaude.dto.studyyear;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

/**
 * DTO para actualizar un StudyYear existente
 */
@Value
@Builder
public class StudyYearUpdateDto {

    @NotBlank(message = "Name cannot be empty")
    @Size(max = 16, message = "Name cannot exceed 16 characters")
    String name;

    @NotNull(message = "Level is required")
    Integer level;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    String description;
}
