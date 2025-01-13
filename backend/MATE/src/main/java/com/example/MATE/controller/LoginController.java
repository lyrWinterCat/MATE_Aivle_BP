package com.example.MATE.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
public class LoginController {
    @GetMapping("/login")
    public String login(){
        return "login/login";
    }

    @GetMapping("/membership")
    public String membership(){
        return "login/membership";
    }
}
