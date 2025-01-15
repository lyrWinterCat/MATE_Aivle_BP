package com.example.MATE.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Appconfig {

    @Getter
    private static String serverDomain;

    @Value("${domain}")
    public void setServerDomain(String serverDomain) {
        Appconfig.serverDomain = serverDomain;

        // 디버깅 로그
        System.out.println("====================================");
        System.out.println("===> Server Domain : " + Appconfig.serverDomain);
        System.out.println("====================================");
    }

}
