package com.example.MATE.controller;

import com.example.MATE.dto.UserDto;
import com.example.MATE.model.Department;
import com.example.MATE.repository.DepartmentRepository;
import com.example.MATE.repository.UserRepository;
import com.example.MATE.service.LoginService;
import com.example.MATE.service.UserService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    //메인페이지(로그인페이지)이동
    @GetMapping("/")
    public String index(){
        return "login/index";
    }

    //로그인 페이지이동
    @GetMapping("/signIn")
    public String login(){
        return "login/index";
    }

    //회원가입 페이지이동
    @GetMapping("/signUp")
    public String signup(){
        System.out.println(">>> 회원가입-페이지이동[LoginController]");
        return "/login/membership";
    }

    //회원가입
    @PostMapping("/signUp")
    public String signup(@ModelAttribute("user")UserDto userDto,
                         RedirectAttributes redirectAttributes,
                         HttpSession session){
        System.out.println(">>> 회원가입[LoginController]");

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

}
