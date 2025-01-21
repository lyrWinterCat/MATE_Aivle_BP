package com.example.MATE.controller;

import com.example.MATE.dto.MeetingLogDto;
import com.example.MATE.service.UserService;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/meetingList")
    public String meetingList(Model model){
        List<MeetingLogDto> meetingLogs = userService.getMeeetingLogs();
        model.addAttribute("meetingLogs", meetingLogs);
        return "user/meetingList";
    }

    @GetMapping("/speechLog")
    public String speechLog() {
        return "/user/speechLog";
    }

    @GetMapping("/userFix")
    public String userFix(){
        return "user/userFix";
    }

    // userFix 페이지에서 "새 글 작성"이라든지 버튼 누르면 userFix/write 주소로 이동
    @GetMapping("/userFix/write")
    public String userFixWrite(){
        return "user/write";
    }

    @GetMapping("/userMain")
    public String userMain(){
        return "user/userMain";
    }

    @GetMapping("/screenShare")
    public String screenShare(){ return "user/screenShare"; }

}
