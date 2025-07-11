
package com.acainfo.backendclaude.dto.major;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;

/**
 * DTO detallado para Major - incluye listas de entidades relacionadas
 */
@Value
@Builder
public class MajorDetailDto {
    Integer id;
    String name;
    String description;
    Instant createdAt;
    List<StudentBasicDto> students;
    List<SubjectBasicDto> subjects;

    /**
     * DTO básico para Student dentro de MajorDetailDto
     */
    @Value
    @Builder
    public static class StudentBasicDto {
        Integer id;
        String name;
        String email;
        Boolean isActive;
        String studyYearName;
    }

    /**
     * DTO básico para Subject dentro de MajorDetailDto
     */
    @Value
    @Builder
    public static class SubjectBasicDto {
        Integer id;
        String name;
        Integer monthlyPrice;
        String studyYearName;
    }
}
