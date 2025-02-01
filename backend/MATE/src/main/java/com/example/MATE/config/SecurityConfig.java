package com.example.MATE.config;

import com.example.MATE.Handler.LoginAuthenticationFailureHandler;
import com.example.MATE.Handler.LoginAuthenticationSuccessHandler;
import com.example.MATE.service.GoogleOAuth2UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import java.util.Collection;

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

                // 로그인 안해도 이미지, css, js 가 적용되도록 수정
                .requestMatchers(
                        "/",
                        "/error",
                        "/favicon.ico",
                        "/css/**",
                        "/js/**",
                        "/img/**",  // 이미지 경로 추가
                        "/static/**",  // static 리소스 전체 추가
                        "/aboutus"
                );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //인증제외URL설정
        http.csrf(csrf -> csrf

               // 로그인 안해도 이미지, css, js 가 적용되도록 수정
                .ignoringRequestMatchers(
                        "/signIn**",
                        "/login",
                        "/signUp**",
                        "/signOut",
                        "/oauth2/authorization/**",
                        "/css/**",
                        "/js/**",
                        "/img/**",  // 이미지 경로 추가
                        "/static/**",  // static 리소스 전체 추가
                        "/aboutus",
                        "/admin/**",
                        "/user/**",
                        "/meeting/**",
                        "/toxic/**"
                        //공통인증에서 /user/**에 권한부여해도 CSRF가 설정되어있으면 접근이 안되어 일단 둘다 써둠
                )
        );
        //공통인증
        http.authorizeHttpRequests(auth -> auth

                // 로그인 안해도 이미지, css, js 가 적용되도록 수정
                .requestMatchers(
                        "/signIn",
                        "/signUp",
                        "/signOut",
                        "/error/**",
                        "/css/**",
                        "/js/**",
                        "/img/**",  // 이미지 경로 추가
                        "/static/**",  // static 리소스 전체 추가
                        "/aboutus"
                ).permitAll()
                .requestMatchers("/user/**",
                        "/toxic/**",
                        "/meeting/**").hasAuthority("USER")
                .requestMatchers("/admin/**",
                        "/toxic/**",
                        "/meeting/**").hasAuthority("ADMIN")
                .anyRequest().authenticated()
        );
        //일반로그인 인증
        http.formLogin(form -> form
                .loginPage("/signIn") //로그인 페이지
                .loginProcessingUrl("/login") //로그인 프로세스 : UserSecurityDetailsService 에서 처리, 로그인폼 action과 동일해야함
                .usernameParameter("email")
                .passwordParameter("password")
                .failureUrl("/signIn?error=true")
                .successHandler(authenticationSuccessHandler())
                .failureHandler(authenticationFailureHandler())
                .permitAll()
        );
        //구글로그인 인증
        http.oauth2Login(auth -> auth
                .loginPage("/signIn")
                .userInfoEndpoint(userInfo -> userInfo
                        .userService(oAuth2UserService())
                )
                .successHandler(authenticationSuccessHandler())
                .failureHandler(authenticationFailureHandler())
        );
        //로그아웃
        http.logout(logout -> logout
                .logoutUrl("/signOut")
                .logoutSuccessUrl("/signIn")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .clearAuthentication(true)
                .permitAll()
        );
        //세션관리
        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .invalidSessionUrl("/signIn")
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
                .expiredUrl("/signIn?error=session_expired")
        );
        //SpringSecurity exception 잡기
        http.exceptionHandling(exception -> exception
                .accessDeniedHandler(ExceptionAccessDeniedHandler())
        );

        return http.build();
    }

    //SpringSecurity Exception 잡기
    @Bean
    public AccessDeniedHandler ExceptionAccessDeniedHandler(){
        return (request, response,
        accessDeniedException) -> {
            System.out.println(">>> [SecurityConfig] 403 Forbidden 접근 거부됨  : "+request.getRequestURI());
            HttpSession session = request.getSession();
            //현재 인증자 정보
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if(authentication != null && authentication.isAuthenticated()) {
                Object pricipal = authentication.getPrincipal();
                System.out.println(">>> [SecurityConfig] 현재 로그인된 사용자 : " + authentication.getName());

                //사용자 권한 확인
                Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

                for (GrantedAuthority authority : authorities) {
                    System.out.println(">>> [SecurityConfig] 현재 사용자권한 : " + authority.getAuthority());
                }

                session.setAttribute("errorMessage", "접근 권한이 없습니다.");
                System.out.println(">>> [SecurityConfig] 현재 사용자는 접근권한이 없습니다.");

                //권한에 따른 페이지 리다이렉트
                if (authorities.stream().anyMatch(auth -> auth.getAuthority().equals("admin"))) {
                    //관리자인 경우
                    response.sendRedirect("/error/403");
                } else {
                    //유저인 경우
                    response.sendRedirect("/error/403");
                }
            }else{
                session.setAttribute("errorMessage","로그인이 필요합니다.");
                response.sendRedirect("/signIn?error=access_denied");
            }
        };
    }
    //구글로그인 성공
    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService(){
        return new GoogleOAuth2UserService();
    }
    //로그인 성공
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler(){
        return new LoginAuthenticationSuccessHandler();
    }
    //로그인 실패
    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler(){
        return new LoginAuthenticationFailureHandler();
    }
    //보안설정
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}