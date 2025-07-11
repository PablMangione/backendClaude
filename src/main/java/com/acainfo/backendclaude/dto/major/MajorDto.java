package com.acainfo.backendclaude.dto.major;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * DTO básico para Major - usado para listados y operaciones simples
 */
@Value
@Builder
public class MajorDto {
    Integer id;
    String name;
    String description;
    Instant createdAt;
}
