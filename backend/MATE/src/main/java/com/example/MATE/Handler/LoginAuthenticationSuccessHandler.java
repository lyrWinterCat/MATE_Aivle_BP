package com.example.MATE.Handler;

import com.example.MATE.model.GoogleOAuth2User;
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
import org.springframework.security.oauth2.core.user.OAuth2User;
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
        //구글 로그인 성공처리(Oauth2)
        else if(principal instanceof OAuth2User){
            OAuth2User oauth2User = (OAuth2User) principal;
            if(oauth2User instanceof GoogleOAuth2User){
                GoogleOAuth2User googleUser = (GoogleOAuth2User) oauth2User;
                email = googleUser.getEmail();
                System.out.println(">>> [LoginAuthenticationSuccessHandler]구글 API 인증 성공 :"+email);
                //구글 로그인 계정(이메일)의 DB 존재여부 확인
                if(userRepository.findByEmail(email).isEmpty()){
                    //신규 : 회원가입 페이지로 이동
                    request.getSession().invalidate();
                    request.setAttribute("signupEmail",email);
                    request.setAttribute("signupName",googleUser.getUserName());
                    request.getRequestDispatcher("/signUp").forward(request, response);
                    System.out.println(">>> [LoginAuthenticationSuccessHandler] 구글 API 인증 - 신규 회원");
                    return;
                }else{
                    //기존 : 유저메인페이지로 이동
                    System.out.println(">>> [LoginAuthenticationSuccessHandler] 구글 API 인증 - 기존 회원");
                }
            }
        }
        //UserSecurityDetail 또는 OAuth2User가 아닌 경우
        else{
            System.out.println(">>> [LoginAuthenticationSuccessHandler] 알 수 없는 사용자 : "+principal.getClass().getName());
        }
        //로그인 성공 시 세션에 사용자 정보 저장
        if(email != null){
            Optional<User> userOptional = userRepository.findByEmail(email);
            if(userOptional.isPresent()){
                User user = userOptional.get();
                //SecurityContextHolder에서 직접 센션관리함
//                session.setAttribute("user",user); //세션에 정보 저장
//                session.setAttribute("userEmail",user.getEmail());
//                session.setAttribute("userName",user.getName());
//                session.setAttribute("userRole",user.getRole().name());
                System.out.println(">>> [LoginAuthenticationSuccessHandler] 세션 설정 완료 : "+user.getEmail()+"/"+user.getRole().name());

                //role에 따른 다른 페이지 이동
                if(user.getRole() == User.Role.ADMIN){
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
