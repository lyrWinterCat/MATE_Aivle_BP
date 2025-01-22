package com.example.MATE.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/error")
public class ErrorController {

    @GetMapping("/403")
    public String accessDeniedPage(Model model, HttpSession session) {
        System.out.println(">>> 403 에러 페이지 접근");

        String errorMessage = (String) session.getAttribute("errorMessage");

        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
            session.removeAttribute("errorMessage"); //
        }

        session.invalidate();
        //springsecurity 인증정보 제거
        SecurityContextHolder.clearContext();

        return "error/error"; //
    }
}
