package com.example.MATE.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MainController {
    @GetMapping("/")
    public String mainPage(){
        return "user/dashboard";
    }

    @GetMapping("/meeting")
    public String meeting(){
        return "user/meeting";
    }
}
