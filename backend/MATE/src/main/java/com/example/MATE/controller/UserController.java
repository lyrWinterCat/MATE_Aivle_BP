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

        //세션에서 사용자 정보 가져오기
        String email = (String) session.getAttribute("userEmail");
        String userName = (String) session.getAttribute("userName");
        String userRole = (String) session.getAttribute("userRole");

        //로그인 여부 확인
        if(email == null){
            System.out.println(">>> 세션에 이메일 없음. 로그인페이지로 이동");
            model.addAttribute("errorMessage","로그인이 필요합니다.");
            return "/signIn";
        }
        System.out.println(">>> 유저 메인 페이지 로드 완료!");
        return "user/userMain";
    }

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

    @GetMapping("/screenShare")
    public String screenShare(){ return "user/screenShare"; }

}
