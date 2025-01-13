package com.example.MATE.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "toxicity_log")
public class ToxicityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "toxicity_id")
    private Integer toxicityId;

    @ManyToOne
    @JoinColumn(name = "log_id", nullable = false)
    private SpeechLog speechLog;

    @ManyToOne
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "corrected", nullable = false)
    private Boolean corrected;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}