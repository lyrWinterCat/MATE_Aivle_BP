package com.example.screenshare.service;

import com.example.screenshare.model.ScreenData;
import org.springframework.stereotype.Service;

@Service
public class ScreenShareService {
    public ScreenData proceesScreenData(ScreenData screenData) {
        screenData.setTimestamp(System.currentTimeMillis());
        return screenData;
    }
}
