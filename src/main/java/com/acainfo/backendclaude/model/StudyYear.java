package com.acainfo.backendclaude.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "study_years")
public class StudyYear {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 16)
    @NotNull
    @Column(name = "name", nullable = false, length = 16)
    private String name;

    @NotNull
    @Column(name = "level", nullable = false)
    private Integer level;

    @Size(max = 255)
    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "studyYear")
    @Builder.Default
    private Set<Student> students = new LinkedHashSet<>();

    @OneToMany(mappedBy = "studyYear")
    @Builder.Default
    private Set<Subject> subjects = new LinkedHashSet<>();

}