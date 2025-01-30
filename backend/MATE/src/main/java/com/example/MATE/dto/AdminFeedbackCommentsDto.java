package com.example.MATE.dto;

import com.example.MATE.model.AdminFeedback;
import com.example.MATE.model.AdminFeedbackComments;
import com.example.MATE.model.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class AdminFeedbackCommentsDto {

    private Integer commentId;
    private Integer feedbackId;
    private Integer userId;
    private String userName;
    private String content; //댓글임
    private String createdAt;
    private Integer parentComment;

    //날짜 포맷 설정
    private static final DateTimeFormatter fomatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    //entity -> DTO
    public static AdminFeedbackCommentsDto fromEntity(AdminFeedbackComments comment){
        AdminFeedbackCommentsDto dto = new AdminFeedbackCommentsDto();
        dto.setCommentId(comment.getCommentId());
        dto.setFeedbackId(comment.getFeedback().getFeedbackId());
        dto.setUserId(comment.getUser().getUserId());
        dto.setUserName(comment.getUser().getName());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt().format(fomatter));
        dto.setParentComment(comment.getParentComment());

        return dto;
    }

    //DTO -> entity
    public AdminFeedbackComments toEntity(User user, AdminFeedback feedback){
        AdminFeedbackComments comments = new AdminFeedbackComments();
        comments.setUser(user);
        comments.setFeedback(feedback);
        comments.setContent(this.content);
        comments.setCreatedAt(LocalDateTime.now());
        comments.setParentComment(this.parentComment);
        return comments;
    }
}
