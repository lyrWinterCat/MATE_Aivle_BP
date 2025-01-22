package com.example.MATE.Handler;

import com.example.MATE.model.User;
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
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
public class LoginAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Autowired
    private UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException
    {
        HttpSession session = request.getSession();
        Object principal = authentication.getPrincipal(); //사용자 정보
        String email = null;
        String redirectUrl = "/user/userMain";

        //일반 로그인 성공처리(UserSecurityDetails)
        if(principal instanceof UserSecurityDetails){
            UserSecurityDetails userSecurityDetails = (UserSecurityDetails) principal;
            email = userSecurityDetails.getUsername();
            System.out.println(">>> [LoginAuthenticationSuccessHandler] 일반 로그인 성공 "+email );
        }

        //UserSecurityDetail 또는 OAuth2User가 아닌 경우
        else{
            System.out.println(">>> [LoginAuthenticationSuccessHandler] 알 수 없는 사용자 : "+principal.getClass().getName());
            email = null;
        }
        //로그인 성공 시 세션에 사용자 정보 저장
        if(email != null){
            Optional<User> userOptional = userRepository.findByEmail(email);
            if(userOptional.isPresent()){
                User user = userOptional.get();

                session.setAttribute("user",user); //세션에 정보 저장
                session.setAttribute("userEmail",user.getEmail());
                session.setAttribute("userName",user.getName());
                session.setAttribute("userRole",user.getRole().name());

                System.out.println(">>> [LoginAuthenticationSuccessHandler] 세션 설정 완료 : "+user.getEmail()+"/"+user.getRole().name());

                //role에 따른 다른 페이지 이동
                if(user.getRole().name().equals("admin")){
                    redirectUrl = "/admin/adminMain";
                }
            }
            response.sendRedirect(redirectUrl);
        }else{
            System.out.println(">>> [LoginAuthenticationSuccessHandler] 로그인 실패-이메일을 가져올 수 없음");
            response.sendRedirect("/signIn?error=true");
        }
    }
}
