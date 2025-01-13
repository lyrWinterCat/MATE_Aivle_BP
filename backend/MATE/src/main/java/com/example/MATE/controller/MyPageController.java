package com.example.MATE.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MyPageController {
    @GetMapping("/meeting/list")
    public String mypageMeetingList(){
        return "/user/mypage/meetingList";
    }

    @GetMapping("/meeting")
    public String mypageMeeting(){
        return "/user/mypage/meeting";
    }

    @GetMapping("/log/list")
    public String mypageLogList(){
        return "/user/mypage/logList";
    }

    @GetMapping("/log")
    public String mypageLog(){
        return "/user/mypage/log";
    }

    @GetMapping("/summary")
    public String mypageSummary(){
        return "/user/mypage/summary";
    }
}
