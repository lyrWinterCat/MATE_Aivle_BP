package com.example.MATE.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")  // 여기를 변경
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")

                // 로그인 안해도 이미지, css, js 가 적용되도록 수정
                .addResourceLocations(
                        "classpath:/static/",
                        "classpath:/public/",
                        "classpath:/resources/",
                        "classpath:/META-INF/resources/"
                );

        // "file" 폴더의 파일을 정적 리소스로 제공하도록 설정
        // 클라이언트가 '/file/{파일명}' URL로 접근하면, 서버의 'file/' 폴더에 있는 파일을 제공
        registry.addResourceHandler("/file/**")
                .addResourceLocations("file:file/");

    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.jsp("/templates/meeting/", ".html");
    }

}
