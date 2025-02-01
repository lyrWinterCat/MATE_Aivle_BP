package com.example.MATE.service;

import com.example.MATE.dto.ToxicityLogDto;
import com.example.MATE.model.ToxicityLog;
import com.example.MATE.repository.ToxicityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ToxicityLogService {
    private final ToxicityLogRepository toxicityLogRepository;

    //특정 미팅에서 특정 사용자 독성로그 조회
    @Transactional
    public List<ToxicityLogDto> getToxicityLogsByMeetingAndUser(Integer meetingId, Integer userId) {
        List<ToxicityLog> toxicityLogs = toxicityLogRepository.findWithSpeechLogByMeetingAndUser(meetingId, userId);

        // Entity -> DTO 변환
        return toxicityLogs.stream()
                .map(ToxicityLogDto::fromEntity)
                .collect(Collectors.toList());
    }
}
