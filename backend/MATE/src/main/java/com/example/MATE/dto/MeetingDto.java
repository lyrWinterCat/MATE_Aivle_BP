package com.example.MATE.dto;

import com.example.MATE.model.Meeting;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class MeetingDto {

    private Integer meetingId;
    private String meetingName;

    //정정게시판-독성조회에서 사용
    //Entity -> DTO
    public static MeetingDto fromEntity(Meeting meeting){
        MeetingDto dto = new MeetingDto();
        dto.setMeetingId(meeting.getMeetingId());
        dto.setMeetingName(meeting.getMeetingName());
        return dto;
    }
}
