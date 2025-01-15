package com.example.MATE.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {
    @GetMapping("/docs")
    public String docs(){
        return "docs";
    }
}
