package com.acainfo.backendclaude.dto.studyyear;

import lombok.Builder;
import lombok.Value;

/**
 * DTO básico para StudyYear - usado para listados y operaciones simples
 */
@Value
@Builder
public class StudyYearDto {
    Integer id;
    String name;
    Integer level;
    String description;
}