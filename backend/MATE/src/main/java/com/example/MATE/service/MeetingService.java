package com.example.MATE.service;

import com.example.MATE.model.Meeting;
import com.example.MATE.model.ScreenData;
import com.example.MATE.repository.MeetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;

    public ScreenData processScreenData(ScreenData screenData) {
        screenData.setTimestamp(System.currentTimeMillis());
        return screenData;
    }

    public List<Meeting> getAllMeetings() {
        return meetingRepository.findAllMeetings();
    }
}
