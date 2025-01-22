package com.example.MATE.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserSecurityDetails implements UserDetails {

    private final String email;
    private final String password;
    private final User.Role role;


    public UserSecurityDetails(String email, String password, User.Role role){
        this.email = email;
        this.password = password;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }
    /*
        계정상태 관련 메서드 - 계정이 유효한지, Spring Security가 로그인 가능 여부 판단시 사용
        - 모든 값이 true이므로, 계정이 만료되지 않고 잠기지않고 사용가능함.
        - 필요하면 DB 연동을 이용하여 동적 변경이 가능(현재 DB에 없음)
    */
    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true;  }

    @Override
    public boolean isEnabled() { return true;  }
}
