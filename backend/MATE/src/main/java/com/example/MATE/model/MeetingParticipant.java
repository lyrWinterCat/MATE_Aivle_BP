package com.example.MATE.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "meeting_participant")
public class MeetingParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participant_id")
    private Integer participantId;

    @ManyToOne
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "is_recording", nullable = false, columnDefinition = "TinyInt DEFAULT 0")
    private boolean isRecording;
}