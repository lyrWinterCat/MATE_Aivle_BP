package com.example.MATE.controller;

import com.example.MATE.dto.MeetingLogDto;
import com.example.MATE.model.ScreenData;
import com.example.MATE.service.MeetingService;
import com.example.MATE.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/mypage/log/search")
    public String userLogSearch(Model model){
        List<MeetingLogDto> meetingLogs = userService.getMeeetingLogs();
        model.addAttribute("meetingLogs", meetingLogs);
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

}
