package com.example.MATE.service;

import com.example.MATE.model.User;
import com.example.MATE.model.UserSecurityDetails;
import com.example.MATE.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
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
        Optional<User> userOptional = userRepository.findByEmail(username);
        if(userOptional.isEmpty()){
            System.out.println(">>> [UserSecurityDetailsService] 미등록 계정임 : "+username);
            throw new BadCredentialsException("미등록계정/사용자가 존재하지 않음 : "+username);
        }

        User foundUser = userOptional.get();
        System.out.println(">>> 계정 조회 성공"+foundUser.getEmail());

        UserSecurityDetails userSecurityDetails = new UserSecurityDetails(
                foundUser.getEmail(),
                foundUser.getPassword(),
                foundUser.getRole()
        );

        return userSecurityDetails;
    }
}
