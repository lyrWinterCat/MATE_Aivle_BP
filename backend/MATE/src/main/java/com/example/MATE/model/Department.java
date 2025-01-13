package com.example.MATE.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Department")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "department_id")
    private Integer departmentId;

    @Column(name = "department_name", nullable = false)
    private String departmentName;

    @Column(name = "team_name", nullable = false)
    private String teamName;

    @Column(name = "description")
    private String description;
}
