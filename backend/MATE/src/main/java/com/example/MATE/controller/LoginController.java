package com.example.MATE.controller;

import com.example.MATE.dto.UserDto;
import com.example.MATE.model.Department;
import com.example.MATE.model.GoogleOAuth2User;
import com.example.MATE.model.User;
import com.example.MATE.model.UserSecurityDetails;
import com.example.MATE.repository.DepartmentRepository;
import com.example.MATE.repository.UserRepository;
import com.example.MATE.service.LoginService;
import com.example.MATE.service.UserService;
import com.example.MATE.utils.SecurityUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    //메인페이지(로그인페이지)이동
    @GetMapping("/")
     public String index(){ return "login/index"; }

    //로그인 페이지이동
    @GetMapping("/signIn")
    public String login(HttpSession session, Model model){
        System.out.println(">>> [LoginController] 로그인-페이지이동");
        //현재 사용자의 로그인 여부 확인
        String email = SecurityUtils.getCurrentUserEmail();
        if (email != null) {
            System.out.println(">>> [LoginController] 현재 로그인된 사용자 이메일: " + email);
            return "redirect:/user/userMain";
        }

        //세션에서 에러메세지 받기 - 로그인 실패
        String errorMessage = (String) session.getAttribute("errorMessage");
        if(errorMessage != null){
            errorMessage = URLDecoder.decode(errorMessage, StandardCharsets.UTF_8);
            model.addAttribute("errorMessage", errorMessage);
            session.removeAttribute("errorMessage");
        }
        return "login/index";
    }

    //회원가입 페이지이동
    @GetMapping("/signUp")
    public String signup(Model model, HttpServletRequest request){
        System.out.println(">>> [LoginController] 회원가입-페이지이동");
        //현재 사용자의 로그인 여부 확인
        String email = SecurityUtils.getCurrentUserEmail();
        if (email != null) {
            System.out.println(">>> [LoginController] 현재 로그인된 사용자 이메일: " + email);
            return "redirect:/user/userMain";
        }

        //구글 API 인증 성공 후 회원가입
        String signupEmail = (String) request.getAttribute("signupEmail");
        String signupName = (String) request.getAttribute("signupName");
        if(signupEmail != null && signupName != null){
            model.addAttribute("signupEmail",signupEmail);
            model.addAttribute("signupName",signupName);
        }

        return "/login/membership";
    }

    //회원가입
    @PostMapping("/signUp")
    public String signup(@ModelAttribute("user")UserDto userDto,
                         RedirectAttributes redirectAttributes,
                         HttpSession session){
        System.out.println(">>> [LoginController] 회원가입");

        try {
            loginService.signup(userDto);
            session.invalidate(); //세션 초기화 - 바로 로그인 안가기위함
            redirectAttributes.addFlashAttribute("success","성공적으로 회원가입을 완료하였습니다.");
            return "redirect:/signIn";
        }catch (LoginService.LoginException ex){
            redirectAttributes.addFlashAttribute("error",ex.getMessage());
        }catch (Exception ex){
            redirectAttributes.addFlashAttribute("error","회원가입에 실패하였습니다. 다시 시도해주세요.");
        }
        return "redirect:/signIn";
    }

    //로그아웃
    @PostMapping("/signOut")
    public String signout(HttpSession session, Model model){
        System.out.println(">>> 로그아웃 ");
        session.invalidate();
        //SpringSecurity 인증정보 제거
        SecurityContextHolder.clearContext();

        return "/signIn";
    }
}
