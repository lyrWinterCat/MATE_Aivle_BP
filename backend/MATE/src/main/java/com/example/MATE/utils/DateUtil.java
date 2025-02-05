package com.example.MATE.utils;

import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Component;

import javax.xml.crypto.Data;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
    private DateUtil(){}

    public static String format(LocalDateTime dateTime){
        return dateTime.format(formatter);
    }

    public static String dateFormat(LocalDateTime dateTime){
        return dateTime.format(dateFormatter);
    }

    public static String timeFormat(LocalDateTime dateTime){
        return dateTime.format(timeFormatter);
    }

}
