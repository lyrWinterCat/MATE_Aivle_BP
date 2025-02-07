package com.example.MATE.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

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
    @ToString.Exclude //무한루프 에러 발생해서 방지코드 넣음-문제시 삭제!
    private Meeting meeting;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude //무한루프 에러 발생해서 방지코드 넣음-문제시 삭제!
    private User user;

    @Column(name = "is_recording", nullable = false, columnDefinition = "TinyInt DEFAULT 0")
    private boolean isRecording;

    @Column(name = "is_attending", nullable = false, columnDefinition = "TinyInt DEFAULT 1")
    private boolean isAttending;
}