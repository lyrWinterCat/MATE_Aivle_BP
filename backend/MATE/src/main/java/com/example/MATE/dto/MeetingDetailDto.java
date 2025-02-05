package com.example.MATE.dto;

import com.example.MATE.model.Meeting;
import com.example.MATE.model.Summary;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MeetingDetailDto {
    private Integer meetingId;
    private String meetingName;
    private String meetingTime;
    private String endTime;
    private String createdAt;
    private String url;
    private String filepath;
    private String participants;

    // 추가된 요약 관련 필드
    private String summaryTopic;
    private String summaryPositiveNegative;
    private String todoList;
    private String summaryTotal;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static MeetingDetailDto fromEntity(Meeting meeting, Summary summary) {
        String participantNames = meeting.getMeetingParticipants().stream()
                .map(mp -> mp.getUser().getName())
                .collect(Collectors.joining(", "));

        return new MeetingDetailDto(
                meeting.getMeetingId(),
                meeting.getMeetingName(),
                meeting.getCreatedAt() != null ? meeting.getCreatedAt().format(formatter) : "",
                meeting.getEndTime() != null ? meeting.getEndTime().format(formatter) : "",
                meeting.getCreatedAt() != null ? meeting.getCreatedAt().format(formatter) : "",
                meeting.getUrl(),
                meeting.getFilepath(),
                participantNames,
                summary != null ? summary.getSummaryTopic() : "",
                summary != null ? summary.getSummaryPositiveNegative() : "",
                summary != null ? summary.getTodoList() : "",
                summary != null ? summary.getSummaryTotal() : ""
        );
    }
}
