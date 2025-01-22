package com.example.MATE.utils;

import com.example.MATE.model.GoogleOAuth2User;
import com.example.MATE.model.UserSecurityDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    //현재 Spring Security 인증된 사용자 정보 가져오기
    public static String getCurrentUserEmail(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = "";
        //인증정보가 없는 경우
        if(authentication == null){
            System.out.println(">>> [SecurityUtils] SecurityContext에 인증정보가 없음.");
            return null;
        }
        //인증된 사용자가 아닐 경우
        if(!authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())){
            System.out.println(">>> [SecurityUtils] 인증된 사용자가 아님.");
            return null;
        }

        Object principal = authentication.getPrincipal();
        //일반 로그인 사용자
        if(principal instanceof UserSecurityDetails userSecurityDetails){
            email =  userSecurityDetails.getUsername();
            System.out.println(">>> [SecurityUtils] "+email);
            return email;
        }
        //구글 로그인 사용자
        else if(principal instanceof GoogleOAuth2User googleOAuth2User){
            email = googleOAuth2User.getEmail();
            System.out.println(">>> [SecurityUtils] "+email);
            return email;
        }
        // 🔹 알 수 없는 인증 정보일 경우
        return null;
    }
}
