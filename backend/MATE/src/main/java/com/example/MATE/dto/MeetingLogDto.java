package com.example.MATE.dto;

import com.example.MATE.model.Meeting;
import com.example.MATE.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class MeetingLogDto {
    private String meetingName;
    private String meetingTime;
    private String participants;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static MeetingLogDto fromEntity(Meeting meeting, List<User> participants) {
        // 회의의 참여자들을 , 로 구분하여 연결
        String participantNames = participants.stream().map(User::getName).collect(Collectors.joining(", "));

        return new MeetingLogDto(
                meeting.getMeetingName(),
                meeting.getCreatedAt().format(formatter),
                participantNames
        );
    }
}
