package com.example.MATE.repository;

import com.example.MATE.model.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

// Meeting 정보를 불러오기 위한 query derived method 작성
public interface MeetingRepository extends JpaRepository<Meeting, Integer> {
    @Query(value = "SELECT * FROM meeting ORDER BY start_time DESC", nativeQuery = true) // JPQL 이 아닌 native query 사용
    List<Meeting> findAllMeetings();
}
