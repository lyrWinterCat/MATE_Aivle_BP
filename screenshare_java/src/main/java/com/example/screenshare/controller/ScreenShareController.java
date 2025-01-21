package com.example.screenshare.controller;

import com.example.screenshare.model.ScreenData;
import com.example.screenshare.service.ScreenShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
@RequestMapping("/meeting")
public class ScreenShareController {

    @Autowired
    private final ScreenShareService screenShareService;

    @GetMapping("/client")
    public String showClientPage() {
        return "meeting/client";
    }


    @GetMapping("/host")
    public String screenShareHost(){
        return "meeting/host";
    }

    @GetMapping("/test")
    public String screenShareTest(){
        return "meeting/test";
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
