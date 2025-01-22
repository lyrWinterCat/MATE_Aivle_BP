package com.example.MATE.service;

import com.example.MATE.model.GoogleOAuth2User;
import com.example.MATE.model.User;
import com.example.MATE.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class GoogleOAuth2UserService extends DefaultOAuth2UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException{
        System.out.println(">>> [GoogleOAuth2UserService] 구글 로그인 실행! ");
        
        //구글 로그인 사용자 객체 가져옴
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        try {
            //구글 로그인 완료
            return processOAuth2User(oAuth2User);
        }catch(Exception ex){
            throw new OAuth2AuthenticationException(new OAuth2Error("oauth2_error","Error processing OAuth2 user : "+ex.getMessage(), null), ex);
        }
    }

    //구글 로그인 성공 시 실행
    private OAuth2User processOAuth2User(OAuth2User oauth2User){
        Map<String, Object> attributes = oauth2User.getAttributes();
        if(attributes == null){
            throw new IllegalArgumentException("attributes cannot be null");
        }
        //구글 로그인 메일주소
        String email = (String) attributes.get("email");
        System.out.println(">>> [GoogleOAuth2UserService] google login email : "+email);
        //구글 로그인 이름(닉네임)
        String name = (String) attributes.get("name");
        System.out.println(">>> [GoogleOAuth2UserService] google login name : "+name);

        //구글 로그인 성공 후 DB 에서 실사용자 여부 확인(email 이용)
        Optional<User> existingUser = userRepository.findByEmail(email);

        if(existingUser.isPresent()){
            //DB에 있음. 기존 사용자.
            System.out.println(">>> [GoogleOAuth2UserService] old user");
            User user = existingUser.get();
            System.out.println(">>> [GoogleOAuth2UserService] 사용자 권한 : "+user.getRole());
            return new GoogleOAuth2User(attributes, user.getEmail(), user.getRole());
        }else{
            //DB에 없음. 새로운 사용자.
            System.out.println(">>> [GoogleOAuth2UserService] new user");
            return new GoogleOAuth2User(attributes);
        }
    }
}
