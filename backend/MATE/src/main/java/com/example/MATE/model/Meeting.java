package com.example.MATE.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "meeting")
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meeting_id")
    private Integer meetingId;

    @Column(name = "meeting_name", nullable = false, length = 100)
    private String meetingName;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "url", nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "filepath", nullable = false, columnDefinition = "TEXT")
    private String filepath;

    @Column(name = "last_break_time")
    private LocalDateTime lastBreakTime;
}