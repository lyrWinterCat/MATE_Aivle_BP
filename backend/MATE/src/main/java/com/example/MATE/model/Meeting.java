package com.example.MATE.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

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

    // meeting -> meetingParticipant 로도 참조 가능하도록 등록 (기존에는 반대방향만 가능했음)
    // FetchType.LAZY 설정을 통해, meeting 을 불러올때는 기본적으로 이 필드가 불려오지 않도록 함.
    // mappaedBy = "meeting" 이라는 것은 MeetingParticipant.meeting 필드에서 관리된다는 뜻
    @OneToMany(mappedBy = "meeting", fetch = FetchType.LAZY)
    @ToString.Exclude //무한루프 에러 발생해서 방지코드 넣음-문제시 삭제!
    private List<MeetingParticipant> meetingParticipants;

}