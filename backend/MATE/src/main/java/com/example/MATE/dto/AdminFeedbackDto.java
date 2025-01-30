package com.example.MATE.dto;

import com.example.MATE.model.AdminFeedback;
import com.example.MATE.model.ToxicityLog;
import com.example.MATE.model.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

// 날짜 검색을 위해서 createdAt 컬럼에 포맷팅을 했습니다.
// service 단에서 처리하면 코드가 복잡해지므로 dto 단에서 처리했습니다.
@Data
public class AdminFeedbackDto {
    private Integer feedbackId;
    private Integer userId; //toentity에서 필요
    private String userName;
    private Integer toxicityId;
    private String title;
    private String content;
    private String status; // enum 타입이지만 문자열로 변환해서 저장
    private String createdAt; // 날짜 검색을 위해 포맷팅된 컬럼
    private String filepath;

    // 날짜 포맷팅
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static AdminFeedbackDto fromEntity(AdminFeedback adminFeedback) {
        AdminFeedbackDto adminFeedbackDto = new AdminFeedbackDto();
        adminFeedbackDto.setFeedbackId(adminFeedback.getFeedbackId());
        adminFeedbackDto.setUserName(adminFeedback.getUser().getName());
        adminFeedbackDto.setToxicityId(adminFeedback.getToxicityLog().getToxicityId());
        adminFeedbackDto.setTitle(adminFeedback.getTitle());
        adminFeedbackDto.setContent((adminFeedback.getContent()));
        adminFeedbackDto.setStatus(adminFeedback.getStatus().name()); //enum 타입을 문자열로 변환
        adminFeedbackDto.setCreatedAt(adminFeedback.getCreatedAt().format(formatter));
        adminFeedbackDto.setFilepath(adminFeedback.getFilepath());

        return adminFeedbackDto;
    }
    //DTO -> entity화
    public AdminFeedback toEntity(User user, ToxicityLog toxicityLog){
        AdminFeedback feedback = new AdminFeedback();
        feedback.setUser(user);
        feedback.setToxicityLog(toxicityLog);
        feedback.setTitle(this.title);
        feedback.setContent(this.content);
        feedback.setStatus(AdminFeedback.FeedbackStatus.신청);
        feedback.setCreatedAt(LocalDateTime.now());
        feedback.setFilepath(this.filepath);

        return feedback;
    }
}