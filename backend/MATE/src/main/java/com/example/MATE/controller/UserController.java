package com.example.MATE.controller;

import com.example.MATE.dto.MeetingLogDto;
import com.example.MATE.model.User;
import com.example.MATE.repository.UserRepository;
import com.example.MATE.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    //유저메인페이지
    @GetMapping("/userMain")
    @PreAuthorize("hasAuthority('user')")
    public String userMain(Model model, HttpSession session){
        System.out.println(">>> 유저 메인 페이지 접근");
        //세션에서 에러메세지 받기
        String errorMessage = (String) session.getAttribute("errorMessage");
        if(errorMessage != null){
            model.addAttribute("errorMessage",errorMessage);
            session.removeAttribute("errorMessage");
        }

        System.out.println(">>> 유저 메인 페이지 로드 완료!");

        // 세션의 userName 을 model 에 전달
        String userName = (String) session.getAttribute("userName");
        model.addAttribute("userName", userName);

        return "user/userMain";
    }

    @GetMapping("/meetingList")
    public String meetingList(Model model, HttpSession session){
        List<MeetingLogDto> meetingLogs = userService.getMeetingLogs();
        model.addAttribute("meetingLogs", meetingLogs);

        // 세션의 userName 을 model 에 전달
        String userName = (String) session.getAttribute("userName");
        model.addAttribute("userName", userName);

        return "user/meetingList";
    }

    @GetMapping("/speechLog")
    public String speechLog(Model model, HttpSession session) {
        String userName = (String) session.getAttribute("userName");
        model.addAttribute("userName", userName);
        return "/user/speechLog";
    }

    @GetMapping("/userFix")
    public String userFix(Model model, HttpSession session){
        String userName = (String) session.getAttribute("userName");
        model.addAttribute("userName", userName);
        return "user/userFix";
    }

    // userFix 페이지에서 "새 글 작성"이라든지 버튼 누르면 userFix/write 주소로 이동
    @GetMapping("/userFix/write")
    public String userFixWrite(Model model, HttpSession session){
        String userName = (String) session.getAttribute("userName");
        model.addAttribute("userName", userName);
        return "user/write";
    }

    // 더이상 사용하지 않는 엔드포인트입니다. 확인 후 삭제해주시면 감사하겠습니다.
    // @GetMapping("/screenShare")
    // public String screenShare(){ return "user/screenShare"; }

}
