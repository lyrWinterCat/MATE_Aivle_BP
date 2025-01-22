package com.example.MATE.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;


public class GoogleOAuth2User implements OAuth2User {

    private final Map<String, Object> attributes;
    private final String email;
    private final User.Role role;

    //신규 사용자 생성자(기본역할없음)
    public GoogleOAuth2User(Map<String, Object> attributes){
        this.attributes = attributes;
        this.email = (String) attributes.get("email");
        this.role = User.Role.USER;
    }

    //기존 사용자 생성자(Role 포함)
    public GoogleOAuth2User(Map<String, Object> attributes, String email, User.Role role){
        this.attributes = attributes;
        this.email = email;
        this.role = (role != null) ? role : User.Role.USER; //Role을 항상 String으로 변환
        System.out.println(">>> [GoogleOAuth2User] GoogleOAuth2User 현재 사용자 역할 : "+this.role);
    }

    public String getEmail() {
        return (String) attributes.get("email");
    }

    public String getUserName(){
        return (String) attributes.get("name");
    }

    public String getRoleAsString() {
        System.out.println(">>> [GoogleOAuth2User] getRoleAsString : "+role.name());
        return role.name(); // "USER" 또는 "ADMIN" 등의 String 값을 반환
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getName() {
        return email;
    }
}
