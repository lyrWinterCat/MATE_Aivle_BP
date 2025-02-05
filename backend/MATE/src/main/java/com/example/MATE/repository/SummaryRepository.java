package com.example.MATE.repository;

import com.example.MATE.model.Meeting;
import com.example.MATE.model.Summary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SummaryRepository extends JpaRepository<Summary, Integer> {
    Optional<Summary> findByMeeting(Meeting meeting);
}
