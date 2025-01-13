package com.example.screenshare.controller;

import com.example.screenshare.model.ScreenData;
import com.example.screenshare.service.ScreenShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ScreenShareController {
    private final ScreenShareService screenShareService;

    @MessageMapping("/screen-data")
    @SendTo("/topic/screen-data")
    public ScreenData handleScreenData(ScreenData screenData) {
        return screenShareService.proceesScreenData(screenData);
    }
}
