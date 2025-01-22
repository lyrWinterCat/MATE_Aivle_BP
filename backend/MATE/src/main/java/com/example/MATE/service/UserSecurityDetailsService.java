package com.example.MATE.service;

import com.example.MATE.model.User;
import com.example.MATE.model.UserSecurityDetails;
import com.example.MATE.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserSecurityDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println(">>> [UserSecurityDetailsService] 로그인 시작! ");
        System.out.println(">>> [UserSecurityDetailsService] 조회할 계정 메일 : "+username);

        //이메일을 이용한 사용자 조회
        User foundUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("미등록 계정입니다: " + username));

        System.out.println(">>> [UserSecurityDetailsService] 계정 조회 성공"+foundUser.getEmail());

        UserSecurityDetails userSecurityDetails = new UserSecurityDetails(
                foundUser.getEmail(),
                foundUser.getPassword(),
                foundUser.getRole()
        );

        return userSecurityDetails;
    }
}
