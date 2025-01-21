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

    @GetMapping("/host")
    public String meetingRecorder(){
        return "meeting/host";
    }

    @GetMapping("/client")
    public String meetingUser(){
        return "meeting/client";
    }


    @GetMapping("/view")
    public String meetingView(){
        return "meeting/view";
    }

    @MessageMapping("/signal")
    @SendTo("/topic/public")
    public ScreenData handleSignal(ScreenData signal) {
        return signal;
    }

    @MessageMapping("/ice-candidate")
    @SendTo("/topic/public")
    public ScreenData handleIceCandidate(ScreenData candidate) {
        return candidate;
    }
}
