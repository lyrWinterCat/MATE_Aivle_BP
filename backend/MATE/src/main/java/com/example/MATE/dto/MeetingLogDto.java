package com.example.MATE.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MeetingLogDto {
    private String meetingName;
    private String meetingTime;
    private String participants;
}
