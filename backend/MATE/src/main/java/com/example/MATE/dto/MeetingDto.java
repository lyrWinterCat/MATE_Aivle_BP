package com.example.MATE.dto;

import com.example.MATE.model.Meeting;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MeetingDto {

    private Integer meetingId;
    private String meetingName;
    private String meetingUrl;

    //정정게시판-독성조회에서 사용
    //유저메인 - 회의목록 선택에서 사용
    //Entity -> DTO
    public static MeetingDto fromEntity(Meeting meeting){
        MeetingDto dto = new MeetingDto();
        dto.setMeetingId(meeting.getMeetingId());
        dto.setMeetingName(meeting.getMeetingName());
        dto.setMeetingUrl(meeting.getUrl());
        return dto;
    }
    //DTO -> entity
    public Meeting toEntity(){
        Meeting meeting = new Meeting();
        meeting.setMeetingName(this.meetingName);
        meeting.setUrl(this.meetingUrl);
        meeting.setCreatedAt(LocalDateTime.now());

        return meeting;
    }
}