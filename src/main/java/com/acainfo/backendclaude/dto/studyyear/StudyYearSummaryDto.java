package com.acainfo.backendclaude.dto.studyyear;

import lombok.Builder;
import lombok.Value;

/**
 * DTO con información de resumen para StudyYear - incluye contadores de entidades relacionadas
 */
@Value
@Builder
public class StudyYearSummaryDto {
    Integer id;
    String name;
    Integer level;
    String description;
    Long totalStudents;
    Long totalSubjects;
    Long activeStudents;
    Long totalMajorsWithStudents;
    Double averageSubjectPrice;

    /**
     * Constructor para usar en queries nativas/JPQL
     */
    public StudyYearSummaryDto(Integer id, String name, Integer level, String description,
                               Long totalStudents, Long totalSubjects, Long activeStudents,
                               Long totalMajorsWithStudents, Double averageSubjectPrice) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.description = description;
        this.totalStudents = totalStudents != null ? totalStudents : 0L;
        this.totalSubjects = totalSubjects != null ? totalSubjects : 0L;
        this.activeStudents = activeStudents != null ? activeStudents : 0L;
        this.totalMajorsWithStudents = totalMajorsWithStudents != null ? totalMajorsWithStudents : 0L;
        this.averageSubjectPrice = averageSubjectPrice != null ? averageSubjectPrice : 0.0;
    }
}
