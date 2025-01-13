package com.example.MATE.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/user")
public class UserController {
    @GetMapping("/dashboard")
    public String dashboard(){
        return "user/dashboard";
    }

    @GetMapping("/meeting")
    public String meeting() {
        return "user/meeting";
    }
}
