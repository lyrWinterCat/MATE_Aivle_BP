package com.example.MATE.Handler;

import com.example.MATE.model.UserSecurityDetails;
import com.example.MATE.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class LoginAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException
    {
        System.out.println(">>> [LoginAuthenticationFailureHandler] 로그인 실패 : "+exception.getMessage());
        String errorMessage = exception.getMessage();

        //요청에 실패 메세지 추가
        request.setAttribute("error",errorMessage);
        String encodeErrorMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);

        //로그인 페이지로 리다이렉션
        response.sendRedirect("/signIn?error="+encodeErrorMessage);
    }

}
