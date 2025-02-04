package com.example.MATE.repository;

import com.example.MATE.model.SpeechLog;
import com.example.MATE.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserId(Integer userId);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // 유저 ID 를 받아 해당 유저가 발화한 모든 발화 로그를 반환
    @Query("SELECT s FROM SpeechLog s WHERE s.user.userId = ?1") // 엔티티 매핑 실패 방지를 위한 JPQL 사용
    Page<SpeechLog> findSpeechLogsByUserId(Integer userId, Pageable pageable);

    // 서버 사이드 필터링
    // 유저 ID 를 받아 해당 유저가 발화한 모든 발화 로그를 반환
    @Query("SELECT s, " +
            "       CASE WHEN t.toxicityId IS NOT NULL THEN '독성' ELSE '일반' END AS speechType " +
            "FROM SpeechLog s " +
            "LEFT JOIN ToxicityLog t ON s.logId = t.speechLog.logId " +
            "WHERE s.user.userId = :userId " +
            "AND (:startDate IS NULL OR s.timestamp >= :startDate) " +
            "AND (:endDate IS NULL OR s.timestamp <= :endDate) " +
            
            // speechType 이 선택안됐으면 무시
            // "일반"이면 ToxicityLog 에 발화 Id 가 안들어있는 로그들만 가져옴
            // "독성"이면 ToxicityLog 에 발화 Id 가 들어있는 로그들만 가져옴
            "AND (:speechType = '' OR (:speechType = '일반' AND t.toxicityId IS NULL) OR (:speechType = '독성' AND t.toxicityId IS NOT NULL)) " +
            "ORDER BY s.timestamp DESC")
    Page<Object[]> findSpeechLogsByUserIdSSF(
            @Param("userId") Integer userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("speechType") String speechType,
            Pageable pageable);

    // 모든 발화 로그를 반환
    @Query("SELECT s FROM SpeechLog s")
    List<SpeechLog> findAllSpeechLogs();

    // pagination에 사용하기 위해 발화 로그를 가져옴
    @Query("SELECT s FROM SpeechLog s")
    Page<SpeechLog> findAllSpeechLogsWithPaging(Pageable pageable);

    // 서버 사이드 필터링
    // 모든 발화 로그를 반환
    @Query("SELECT s, " +
            "       CASE WHEN t.toxicityId IS NOT NULL THEN '독성' ELSE '일반' END AS speechType " +
            "FROM SpeechLog s " +
            "LEFT JOIN ToxicityLog t ON s.logId = t.speechLog.logId " +
            "WHERE (:startDate IS NULL OR s.timestamp >= :startDate) " +
            "  AND (:endDate IS NULL OR s.timestamp <= :endDate) " +
            "  AND (:speechType = '' OR (:speechType = '일반' AND t.toxicityId IS NULL) OR (:speechType = '독성' AND t.toxicityId IS NOT NULL)) " +
            "ORDER BY s.timestamp DESC")
    Page<Object[]> findAllSpeechLogsSSF(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("speechType") String speechType,
            Pageable pageable);
}
