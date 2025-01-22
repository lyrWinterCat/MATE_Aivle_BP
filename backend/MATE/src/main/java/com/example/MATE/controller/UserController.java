package com.example.MATE.controller;

import com.example.MATE.dto.MeetingLogDto;
import com.example.MATE.model.GoogleOAuth2User;
import com.example.MATE.model.User;
import com.example.MATE.model.UserSecurityDetails;
import com.example.MATE.repository.UserRepository;
import com.example.MATE.service.UserService;
import com.example.MATE.utils.SecurityUtils;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
    @PreAuthorize("hasAuthority('USER')")
    public String userMain(Model model, HttpSession session){
        System.out.println(">>> [UserController/userMain] 유저 메인 페이지 로드");

        //세션에서 사용자 메일가져오기
        String email = SecurityUtils.getCurrentUserEmail();
        //DB에서 사용자 정보 조회-이름과 롤 가져오기 위함
        if (email != null) {
            Optional<User> userOptional = userService.findByEmail(email);
            if (userOptional.isPresent()) { //DB에 있음
                User user = userOptional.get();
                System.out.println(">>> [UserController/userMain] "+user.getName());
                model.addAttribute("userName", user.getName());
                //model.addAttribute("userRole", user.getRole().name());
            }
        }

        //세션에서 에러메세지 받기
        String errorMessage = (String) session.getAttribute("errorMessage");
        if(errorMessage != null){
            errorMessage = URLDecoder.decode(errorMessage, StandardCharsets.UTF_8);
            model.addAttribute("errorMessage", errorMessage);
            session.removeAttribute("errorMessage");
        }
        return "user/userMain";
    }

    // 현재 로그인한 유저가 참여한 모든 회의를 보여주는 페이지
    @GetMapping("/meetingList")
    @PreAuthorize("hasAuthority('USER')")
    public String meetingList(Model model, HttpSession session){

        //세션에서 사용자 메일가져오기
        String email = SecurityUtils.getCurrentUserEmail();
        //DB에서 사용자 정보 조회-이름과 롤 가져오기 위함
        if (email != null) {
            Optional<User> userOptional = userService.findByEmail(email);
            if (userOptional.isPresent()) { //DB에 있음
                User user = userOptional.get();
                Integer userId = user.getUserId();
                //상단에 사용자명 노출
                System.out.println(">>> [UserController/meetingList] "+user.getName());
                model.addAttribute("userName", user.getName());

                //참여 미팅 로그
                List<MeetingLogDto> meetingLogs = userService.getMeetingLogs(userId);
                model.addAttribute("meetingLogs", meetingLogs);
            }
        }

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
    // "정정 요청" 과 같은 버튼이 필요합니다.
    @GetMapping("/userFix/write")
    public String userFixWrite(Model model, HttpSession session){
        String userName = (String) session.getAttribute("userName");
        model.addAttribute("userName", userName);
        return "user/write";
    }
}
