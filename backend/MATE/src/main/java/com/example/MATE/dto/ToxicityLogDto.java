package com.example.MATE.dto;

import com.example.MATE.model.SpeechLog;
import com.example.MATE.model.ToxicityLog;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ToxicityLogDto {
    private Integer toxicityId;
    private String userName;
    private String meetingName;
    private Integer userId;
    private Integer meetingId;
    private Boolean corrected;
    private LocalDateTime updatedAt;

    //독성ID에 해당하는 speechLog가져오기
    private Integer speechLogId;
    private String speechContent;
    private String speechTimestamp;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    //Entity->DTO
    public static ToxicityLogDto fromEntity(ToxicityLog toxicityLog){
        return new ToxicityLogDto(
            toxicityLog.getToxicityId(),
            toxicityLog.getUser().getName(),
            toxicityLog.getMeeting().getMeetingName(),
            toxicityLog.getUser().getUserId(),
            toxicityLog.getMeeting().getMeetingId(),
            toxicityLog.getCorrected(),
            toxicityLog.getUpdatedAt(),
            toxicityLog.getSpeechLog().getLogId(),
            toxicityLog.getSpeechLog().getContent(),
            toxicityLog.getSpeechLog().getTimestamp().format(FORMATTER)
        );
    }
}
