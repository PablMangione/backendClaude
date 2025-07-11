
package com.acainfo.backendclaude.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

/**
 * DTO para actualizar un Major existente
 */
@Value
@Builder
public class MajorUpdateDto {

    @NotBlank(message = "Name cannot be empty")
    @Size(max = 255, message = "Name cannot exceed 255 characters")
    String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    String description;
}