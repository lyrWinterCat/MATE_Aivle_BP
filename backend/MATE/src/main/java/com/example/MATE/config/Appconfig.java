package com.example.MATE.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Appconfig {

    public static String ServerPort;

    @Value("${server.port}")
    public void setServerPort(String server_port) {
        ServerPort = server_port;
        System.out.println("====================================");
        System.out.println("===> Server PORT : " + ServerPort);
        System.out.println("====================================");
    }
}
