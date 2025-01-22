package com.example.MATE.service;

import com.example.MATE.model.Meeting;
import com.example.MATE.model.ScreenData;
import com.example.MATE.repository.MeetingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingService {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final MeetingRepository meetingRepository;

    public void broadcastScreenData(ScreenData screenData) {
        messagingTemplate.convertAndSend("/topic/public", screenData);
    }

    public List<Meeting> getAllMeetings() {
        return meetingRepository.findAllMeetings();
    }

    // 유저 ID 를 받아 해당 유저가 참여한 모든 미팅 ID 를 반환
    public List<Integer> getMeetingIdsByUserId(Integer userId) {
        return meetingRepository.findMeetingIdsByUserId(userId);
    }

    // 미팅 ID 를 받아 해당 미팅의 정보를 반환
    public Meeting getMeetingByMeetingId(Integer meetingId) {
        return meetingRepository.findByMeetingId(meetingId);
    }
}
