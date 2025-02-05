package com.example.MATE.repository;

import com.example.MATE.model.ToxicityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ToxicityLogRepository extends JpaRepository<ToxicityLog, Integer> {
    boolean existsBySpeechLog_LogId(Integer logId);

    //특정 미팅의 특정 사용자 독성 로그 조회
    @Query("SELECT t FROM ToxicityLog t JOIN FETCH t.speechLog WHERE t.meeting.meetingId = :meetingId AND t.user.userId = :userId")
    List<ToxicityLog> findWithSpeechLogByMeetingAndUser(@Param("meetingId") Integer meetingId, @Param("userId") Integer userId);

    @Query("SELECT COUNT(t) FROM ToxicityLog t")
    Long countAllToxicityLog();
}
