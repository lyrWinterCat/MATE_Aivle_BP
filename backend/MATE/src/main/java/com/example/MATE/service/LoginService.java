package com.example.MATE.service;

import com.example.MATE.dto.LoginRequest;
import org.springframework.stereotype.Service;

@Service
public class LoginService {
    public String login(LoginRequest loginRequest) {
        // 로그인 로직 처리
        return "token";
    }
}
