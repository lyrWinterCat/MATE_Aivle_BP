package com.example.MATE.repository;

import com.example.MATE.dto.AdminFeedbackDto;
import com.example.MATE.model.AdminFeedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdminRepository extends JpaRepository<AdminFeedback, Long> {
    @Query(value = "select * from admin_feedback", nativeQuery = true)
    List<AdminFeedback> findFeedbacks();
    
    // 모든 정정 요청을 pagination 을 위해 가져옴
    @Query(value = "select * from admin_feedback ORDER BY created_at DESC", nativeQuery = true)
    Page<AdminFeedback> findAllFeedbacksWithPaging(Pageable pageable);

    // 한 유저의 정정 요청을 모두 가져옴
    // 클라이언트 사이드 필터링 -> 서버 사이드 필터링 수정 (현재 페이지 내에서만 필터링 되던 것을 DB 모든 자료 내에서 필터링해서 새로 pagination 되도록 수정)
    // 시작기간, 종료기간, 상태를 지정하지 않는다면 그 조건은 무시됨
    @Query("SELECT a FROM AdminFeedback a WHERE a.user.userId = :userId " +
            "AND (:startDate IS NULL OR a.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR a.createdAt <= :endDate) " +
            "AND (:status IS NULL OR a.status = :status) " +
            "ORDER BY a.createdAt DESC")
    Page<AdminFeedback> findFeedbacksByUserIdWithFilter(@Param("userId") Integer userId,
                                                        @Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate,
                                                        @Param("status") AdminFeedback.FeedbackStatus status,
                                                        Pageable pageable);
}
