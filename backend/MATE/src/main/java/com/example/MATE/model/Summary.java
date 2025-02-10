package com.example.MATE.model;

import jakarta.persistence.*;
import lombok.Data;
import net.minidev.json.annotate.JsonIgnore;

@Data
@Entity
@Table(name = "summary")
public class Summary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "summary_id")
    private Integer summaryId;

    @OneToOne
    @JoinColumn(name = "meeting_id", nullable = false)
    @JsonIgnore
    private Meeting meeting;

    @Column(name = "summary_topic", columnDefinition = "TEXT", nullable = false)
    private String summaryTopic;

    @Column(name = "summary_positive_negative", columnDefinition = "TEXT", nullable = false)
    private String summaryPositiveNegative;

    @Column(name = "todo_list", columnDefinition = "TEXT", nullable = false)
    private String todoList;

    @Column(name = "summary_total", columnDefinition = "TEXT", nullable = false)
    private String summaryTotal;

    @Column(name = "summary_shared_file", columnDefinition = "TEXT")
    private String summarySharedFile;
}
