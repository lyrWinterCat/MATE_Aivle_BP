package com.example.MATE.repository;

import com.example.MATE.model.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

// Meeting 정보를 불러오기 위한 query derived method 작성
public interface MeetingRepository extends JpaRepository<Meeting, Integer> {
    @Query(value = "SELECT * FROM meeting ORDER BY start_time DESC", nativeQuery = true) // JPQL 이 아닌 native query 사용
    List<Meeting> findAllMeetings();

    // 유저 ID 를 받아 해당 유저가 참여한 모든 미팅 ID 를 반환
    @Query(value = "SELECT meeting_id FROM meeting_participant WHERE user_id = ?1", nativeQuery = true)
    List<Integer> findMeetingIdsByUserId(Integer userId);

    // 미팅 ID 를 받아 해당 미팅의 정보를 반환
    Meeting findByMeetingId(Integer meetingId);
}
