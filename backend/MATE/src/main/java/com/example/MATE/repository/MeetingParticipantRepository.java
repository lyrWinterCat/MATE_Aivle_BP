package com.example.MATE.repository;

import com.example.MATE.model.MeetingParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

// Meeting 참가자 정보를 불러오기 위한 query derived method 작성
public interface MeetingParticipantRepository extends JpaRepository<MeetingParticipant, Integer> {
    @Query(value = "SELECT * FROM meeting_participant WHERE meeting_id = ?1", nativeQuery = true) // 함수의 첫번째 매개변수 (meetingId) 를 ?1 에 매핑
    List<MeetingParticipant> findParticipantsByMeetingId(Integer meetingId);
}
