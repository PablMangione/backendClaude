package com.acainfo.backendclaude.dto.studyyear;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * DTO detallado para StudyYear - incluye listas de entidades relacionadas
 */
@Value
@Builder
public class StudyYearDetailDto {
    Integer id;
    String name;
    Integer level;
    String description;
    List<StudentBasicDto> students;
    List<SubjectBasicDto> subjects;

    /**
     * DTO básico para Student dentro de StudyYearDetailDto
     */
    @Value
    @Builder
    public static class StudentBasicDto {
        Integer id;
        String name;
        String email;
        Boolean isActive;
        String majorName;
    }

    /**
     * DTO básico para Subject dentro de StudyYearDetailDto
     */
    @Value
    @Builder
    public static class SubjectBasicDto {
        Integer id;
        String name;
        Integer monthlyPrice;
        String majorName;
    }
}
