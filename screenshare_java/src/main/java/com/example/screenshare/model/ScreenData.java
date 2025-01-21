package com.example.screenshare.model;

import lombok.Data;

@Data
public class ScreenData {
    private String type;    // 'offer', 'answer', 'candidate'
    private Object payload;
}
