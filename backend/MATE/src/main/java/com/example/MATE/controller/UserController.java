package com.example.MATE.controller;

import com.example.MATE.model.ScreenData;
import com.example.MATE.service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final MeetingService meetingService;

    @GetMapping("/mypage/log/search")
    public String userLogSearch(){
        return "/user/mypage/usersearchlog";
    }

    @GetMapping("/mypage/log")
    public String userLog() {
        return "/user/mypage/userlog";
    }

    @GetMapping("/query/search")
    public String userQuerySearch(){
        return "user/userquery/usersearchquery";
    }

    @GetMapping("/query/write")
    public String userQueryWrite(){
        return "user/userquery/userquerywrite";
    }

    @GetMapping("/dashboard")
    public String userDashboard(){
        return "user/userdashboard";
    }

    @MessageMapping("/screen-data")
    @SendTo("/topic/screen-data")
    public ScreenData handleScreenData(ScreenData screenData) {
        return meetingService.processScreenData(screenData);
    }
}
