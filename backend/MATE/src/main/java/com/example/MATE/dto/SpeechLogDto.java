package com.example.MATE.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SpeechLogDto {
    private String timestamp;
    private String speechType;
    private String userName;
    private String content;
}
