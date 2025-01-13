package com.example.MATE.service;

import com.example.MATE.model.ScreenData;
import org.springframework.stereotype.Service;

@Service
public class MeetingService {
    public ScreenData processScreenData(ScreenData screenData) {
        screenData.setTimestamp(System.currentTimeMillis());
        return screenData;
    }
}
