package com.example.MATE.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/query")
public class QueryController {
    @GetMapping("")
    public String query(){
        return "user/query/search";
    }

    @GetMapping("/write")
    public String queryWrite(){
        return "/user/query/write";
    }
}
