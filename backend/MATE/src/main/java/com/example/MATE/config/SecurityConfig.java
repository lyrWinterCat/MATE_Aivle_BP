package com.example.MATE.config;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


// Spring Security 설정
// spring security 기본 로그인 창이 뜨는 게 싫어서 임시적으로 모두에게 permit 해준거임
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        //security를 적용하지 않을 리소스 추가(error, icon, css, img, js)
        return web -> web.ignoring()
                .requestMatchers("/","/error","/favicon.ico","/*.css","/*.img","/*.js");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //인증제외URL설정
        http.csrf(csrf -> csrf
                .ignoringRequestMatchers("/signIn","/signUp","/signOut","/oauth2/authorization/**")
        );
        //공통인증
        http.authorizeHttpRequests(auth -> auth
                //.requestMatchers("/**").permitAll() //테스트용 모든 URL 권한 오픈
                .requestMatchers("/signIn","/signUp","/signOut","/error/**","/*.css","/*.img","/*.js").permitAll()
                //.requestMatchers("/user/**").hasAuthority("USER")
                //.requestMatchers("/admin/**").hasAuthority("ADMIN")
                //.anyRequest().authenticated()
        );
        return http.build();
    }

    //보안설정
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
