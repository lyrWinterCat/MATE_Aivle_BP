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
}
