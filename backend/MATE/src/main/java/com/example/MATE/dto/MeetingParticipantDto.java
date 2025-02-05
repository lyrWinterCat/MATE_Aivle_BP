package com.example.MATE.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MeetingParticipantDto {
    private Integer participantId; // 참여자 ID
    private String userName; // 사용자 이름
}
