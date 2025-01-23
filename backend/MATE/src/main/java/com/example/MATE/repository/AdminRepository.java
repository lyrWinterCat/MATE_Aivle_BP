package com.example.MATE.repository;

import com.example.MATE.model.AdminFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminRepository extends JpaRepository<AdminFeedback, Long> {
    @Query(value = "select * from admin_feedback", nativeQuery = true)
    List<AdminFeedback> findFeedbacks();

    // 한 유저의 정정 요청을 모두 가져옴
    List<AdminFeedback> findAllByUser_UserId(Integer userId);
}
