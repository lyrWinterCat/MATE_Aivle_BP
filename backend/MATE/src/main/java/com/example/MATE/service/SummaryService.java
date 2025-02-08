package com.example.MATE.service;

import com.example.MATE.dto.SummaryDto;
import com.example.MATE.model.Meeting;
import com.example.MATE.model.Summary;
import com.example.MATE.repository.MeetingRepository;
import com.example.MATE.repository.SummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SummaryService {

    private final SummaryRepository summaryRepository;
    private final MeetingRepository meetingRepository;

    // ✅ meetingId를 통해 Summary 조회 (Meeting 객체를 먼저 찾는 방식)
    @Transactional(readOnly = true)
    public SummaryDto getSummaryByMeetingId(Integer meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("회의를 찾을 수 없습니다."));

        Summary summary = summaryRepository.findByMeeting(meeting)
                .orElseThrow(() -> new RuntimeException("해당 meetingId에 대한 요약이 없습니다."));

        return SummaryDto.fromEntity(summary);
    }

    // ✅ meetingId를 직접 사용하여 Summary 조회하는 방식 (@Query 사용)
    @Transactional(readOnly = true)
    public SummaryDto getSummaryByMeetingIdDirectly(Integer meetingId) {
        Summary summary = summaryRepository.findByMeetingId(meetingId)
                .orElseThrow(() -> new RuntimeException("해당 meetingId에 대한 요약이 없습니다."));

        return SummaryDto.fromEntity(summary);
    }
}
