package com.example.MATE.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RequestMapping("/meeting")
public class MeetingController {
    @GetMapping("/recorder")
    public String meetingRecorder(){
        return "meeting/recordermeeting";
    }

    @GetMapping("/user")
    public String meetingUser(){
        return "meeting/usermeeting";
    }
}
