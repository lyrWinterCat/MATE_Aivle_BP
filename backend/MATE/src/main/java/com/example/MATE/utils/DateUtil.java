package com.example.MATE.utils;

import org.springframework.stereotype.Component;

import javax.xml.crypto.Data;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateUtil(){}

    public static String format(LocalDateTime dateTime){
        return dateTime.format(formatter);
    }

}
