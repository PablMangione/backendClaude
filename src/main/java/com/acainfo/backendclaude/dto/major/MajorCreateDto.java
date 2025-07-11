
package com.acainfo.backendclaude.dto.major;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

/**
 * DTO para crear un nuevo Major
 */
@Value
@Builder
public class MajorCreateDto {

    @NotBlank(message = "Name cannot be empty")
    @Size(max = 255, message = "Name cannot exceed 255 characters")
    String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    String description;
}
