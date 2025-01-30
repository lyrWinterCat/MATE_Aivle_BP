package com.example.MATE.repository;


import com.example.MATE.model.AdminFeedbackComments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentsRepository extends JpaRepository<AdminFeedbackComments, Integer> {


    Optional<AdminFeedbackComments> findFirstByFeedback_FeedbackIdOrderByCreatedAtAsc(Integer feedbackId);
}