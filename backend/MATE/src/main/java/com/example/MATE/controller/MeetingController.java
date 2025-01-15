package com.example.MATE.controller;

import com.example.MATE.model.ScreenData;
import com.example.MATE.service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RequestMapping("/meeting")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    @GetMapping("/recorder")
    public String meetingRecorder(){
        return "meeting/recordermeeting";
    }

    @GetMapping("/user")
    public String meetingUser(){
        return "meeting/usermeeting";
    }

    @MessageMapping("/screen-data")
    @SendTo("/topic/screen-data")
    public ScreenData handleScreenData(ScreenData screenData) {
        return meetingService.processScreenData(screenData);
    }

}
