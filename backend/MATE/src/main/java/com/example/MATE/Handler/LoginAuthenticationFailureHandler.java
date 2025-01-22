package com.example.MATE.Handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.BadCredentialsException;
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
        String errorMessage = exception.getMessage();
        System.out.println(errorMessage);
        if(exception instanceof BadCredentialsException){
            errorMessage = "아이디 또는 비밀번호가 잘못되었습니다.";
        }
        System.out.println(">>> [LoginAuthenticationFailureHandler] 로그인 실패 : "+errorMessage);
        String encodeErrorMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);

        //세션에 에러 메세지 저장(URL에 미표시)
        HttpSession session = request.getSession();
        session.setAttribute("errorMessage", encodeErrorMessage);

        response.sendRedirect("/signIn");
    }

}
