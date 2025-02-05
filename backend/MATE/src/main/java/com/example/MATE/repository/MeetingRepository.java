package com.example.MATE.repository;

import com.example.MATE.model.Meeting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MeetingRepository extends JpaRepository<Meeting, Integer> {
    // Meeting 정보를 불러오기 위한 query derived method 작성
    @Query(value = "SELECT * FROM meeting ORDER BY start_time DESC", nativeQuery = true) // JPQL 이 아닌 native query 사용
    List<Meeting> findAllMeetings();
    // 유저 ID 를 받아 해당 유저가 참여한 모든 미팅 ID 를 반환
    @Query(value = "SELECT meeting_id FROM meeting_participant WHERE user_id = ?1", nativeQuery = true)
    List<Integer> findMeetingIdsByUserId(Integer userId);

    // 특정 사용자 userId를 가진 MeetingParticipant가 속한 Meeting을 페이징
    Page<Meeting> findByMeetingParticipants_User_UserId(Integer userId, Pageable pageable);

    // 미팅 ID 를 받아 해당 미팅의 정보를 반환
    Meeting findByMeetingId(Integer meetingId);

    //미팅URL유무 확인
    Boolean existsByUrl(String url);

    //미팅있니?
    Optional<Meeting> findByUrl(String url);

    // 서버 사이드 필터링을 사용
    // meetingList 에 정보를 바인딩하기 위한 JPQL 문
    // 로그인한 사용자가 포함된 회의의 ID 를 모두 가져와서 그 ID에 해당하는 Meeting 객체만 가져옴
    @Query("SELECT DISTINCT m FROM Meeting m " +

            // 현재 로그인한 사용자가 참여한 회의 ID 찾기
            "WHERE m.meetingId IN ( " +
            "    SELECT mp1.meeting.meetingId FROM MeetingParticipant mp1 " +
            "    WHERE mp1.user.userId = :userId " +
            ") " +

            // 만약 "사원명" 필드에 값이 입력된다면 그 사원이 참여한 회의 ID 찾기
            // AND 조건으로 인해 로그인한 사용자와, 입력한 사원명 모두가 포함된 회의만 가져오게 됨
            "AND m.meetingId IN ( " +
            "    SELECT mp2.meeting.meetingId FROM MeetingParticipant mp2 " +
            "    WHERE (:employeeName = '' OR mp2.user.name LIKE CONCAT('%', :employeeName, '%')) " +
            ") " +

            "AND (:startDate IS NULL OR m.startTime >= :startDate) " +
            "AND (:endDate IS NULL OR m.startTime <= :endDate) " +
            "ORDER BY m.startTime DESC")
    Page<Meeting> findMeetingsByUserIdSSF(
            @Param("userId") Integer userId,
            @Param("employeeName") String employeeName,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // 회의의 총 개수를 세는 함수
    @Query("SELECT COUNT(m) FROM Meeting m")
    Long countAllMeetings();

    // 평균 회의 시간을 구하는 함수
    // end_time 이 null 인, 즉 아직 끝나지 않은 회의들에 대해서는 계산하지 않는다.
    @Query(value = "SELECT AVG(TIMESTAMPDIFF(SECOND, start_time, end_time)) FROM meeting WHERE end_time IS NOT NULL", nativeQuery = true)
    Double findAverageMeetingDuration();

    // 부서별 독성 발언 수를 구하는 함수
    @Query("""
        SELECT d.departmentName, COUNT(t.toxicityId)
        FROM ToxicityLog t
        JOIN User u on t.user.userId = u.userId
        JOIN Department d on u.department.departmentId = d.departmentId
        GROUP BY d.departmentName
    """)
    List<Object[]> findToxicityLogsCountByDepartment();


    @Query("""
    SELECT FUNCTION('DATE', s.timestamp), COUNT(t.toxicityId) 
    FROM ToxicityLog t
    JOIN SpeechLog s ON t.speechLog.logId = s.logId
    WHERE s.timestamp >= :startDate AND s.timestamp <= :endDate
    GROUP BY FUNCTION('DATE', s.timestamp)
    ORDER BY FUNCTION('DATE', s.timestamp) ASC
""")
    List<Object[]> findDailyToxicityLogCount(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);


}
