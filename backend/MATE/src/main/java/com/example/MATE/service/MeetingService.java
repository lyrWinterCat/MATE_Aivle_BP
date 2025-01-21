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
}
