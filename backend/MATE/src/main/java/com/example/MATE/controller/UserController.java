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
    
    // 현재 로그인한 유저가 참여한 모든 회의를 보여주는 페이지
    @GetMapping("/meetingList")
    public String meetingList(Model model, HttpSession session){

        // 세션에서 user 객체 받아온 후 userId 추출
        User user = (User) session.getAttribute("user");
        Integer userId = user.getUserId();

        // 로그인한 유저가 참여한 모든 미팅을 가져옴
        List<MeetingLogDto> meetingLogs = userService.getMeetingLogs(userId);
        model.addAttribute("meetingLogs", meetingLogs);

        // 세션의 userName 을 model 에 전달 (페이지 상단에 사용자 이름 표시를 위함임)
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

    // userFix 페이지에서 "정정 요청"이라든지 버튼 누르면 userFix/write 주소로 이동
    // "정정 요청" 과 같은 버튼이 필요합니다.
    @GetMapping("/userFix/write")
    public String userFixWrite(Model model, HttpSession session){
        String userName = (String) session.getAttribute("userName");
        model.addAttribute("userName", userName);
        return "user/write";
    }
}
