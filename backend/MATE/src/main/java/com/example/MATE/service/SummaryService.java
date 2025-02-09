package com.example.MATE.service;

import com.example.MATE.dto.SummaryDto;
import com.example.MATE.model.Meeting;
import com.example.MATE.model.Summary;
import com.example.MATE.repository.MeetingRepository;
import com.example.MATE.repository.SummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SummaryService {

    private final SummaryRepository summaryRepository;
    private final MeetingRepository meetingRepository;

    //meetingId를 통해 Summary 조회 (Meeting 객체를 먼저 찾는 방식)
    @Transactional(readOnly = true)
    public ResponseEntity<?> getSummaryByMeetingId(Integer meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("회의를 찾을 수 없습니다."));

        Optional<Summary> optionalSummary = summaryRepository.findByMeeting(meeting);

        if (optionalSummary.isEmpty()) {
            System.out.println("해당 meetingId에 대한 요약이 없습니다. (meetingId: " + meetingId + ")");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("요약 데이터가 없습니다.");
        }

        SummaryDto summaryDto = SummaryDto.fromEntity(optionalSummary.get());
        return ResponseEntity.ok(summaryDto);
    }

}
