package com.example.MATE.repository;

import com.example.MATE.model.SpeechLog;
import com.example.MATE.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // 유저 ID 를 받아 해당 유저가 발화한 모든 발화 로그를 반환
    // @Query(value = "SELECT * FROM speech_log WHERE user_id = ?1", nativeQuery = true)
    @Query("SELECT s FROM SpeechLog s WHERE s.user.userId = ?1") // 엔티티 매핑 실패 방지를 위한 JPQL 사용
    List<SpeechLog> findSpeechLogsByUserId(Integer userId);

    // 모든 발화 로그를 반환
    @Query("SELECT s FROM SpeechLog s")
    List<SpeechLog> findAllSpeechLogs();
}
