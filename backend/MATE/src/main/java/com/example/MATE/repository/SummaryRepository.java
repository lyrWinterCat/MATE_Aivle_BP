package com.example.MATE.repository;

import com.example.MATE.model.Meeting;
import com.example.MATE.model.Summary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SummaryRepository extends JpaRepository<Summary, Integer> {
    Optional<Summary> findByMeeting(Meeting meeting);

    @Query("SELECT s FROM Summary s WHERE s.meeting.meetingId = :meetingId")
    Optional<Summary> findByMeetingId(@Param("meetingId") Integer meetingId);
}
