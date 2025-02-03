package com.example.MATE.service;

import com.example.MATE.dto.AdminFeedbackCommentsDto;
import com.example.MATE.dto.AdminFeedbackDto;
import com.example.MATE.dto.RequestDto;
import com.example.MATE.model.AdminFeedback;
import com.example.MATE.model.AdminFeedbackComments;
import com.example.MATE.model.User;
import com.example.MATE.repository.AdminRepository;
import com.example.MATE.repository.CommentsRepository;
import com.example.MATE.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final CommentsRepository commentsRepository;

    public List<RequestDto> getAllRequests() {
        // 모든 요청 가져오기 로직
        return new ArrayList<>();
    }

    public List<AdminFeedbackDto> getFeedbackList() {
        // 모든 피드백 가져오기
        return adminRepository.findFeedbacks().stream()
                .map(AdminFeedbackDto::fromEntity) // 원래 AdminFeedback 타입이었던 원소들을 AdminFeedbackDto 타입으로 변환 (fromEntity 메소드를 사용해서)
                .collect(Collectors.toList()); // List<AdminFeedback> -> List<AdminFeedbackDto>
    }

    public Page<AdminFeedbackDto> getFeedbackListWithPaging(Pageable pageable) {
        Page<AdminFeedback> pagedFeedbacks = adminRepository.findAllFeedbacksWithPaging(pageable);
        return pagedFeedbacks.map(AdminFeedbackDto::fromEntity);
    }

    public Page<AdminFeedbackDto> getFeedbackListSSF(String employeeName, String startDate, String endDate, String statusStr, Pageable pageable) {
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;

        // 하루의 시작은 00:00:00
        if (startDate != null && !startDate.isEmpty()) {
            startDateTime = LocalDate.parse(startDate).atStartOfDay();
        }

        // 하루의 끝은 23:59:59
        if (endDate != null && !endDate.isEmpty()) {
            endDateTime = LocalDate.parse(endDate).atTime(23, 59, 59);
        }

        // status 변환 (String -> enum)
        AdminFeedback.FeedbackStatus status = null;
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                status = AdminFeedback.FeedbackStatus.valueOf(statusStr);
            } catch (IllegalArgumentException e) {
                System.out.println(">>> [AdminService] 잘못된 status 값: " + statusStr);
            }
        }

        Page<AdminFeedback> feedbacks = adminRepository.findFeedbackListSSF(employeeName, startDateTime, endDateTime, status, pageable);
        return feedbacks.map(AdminFeedbackDto::fromEntity);
    }

    public AdminFeedback getFeedbackById(Integer feedbackId) {
        return adminRepository.findById(Long.valueOf(feedbackId))
                .orElseThrow(() -> new IllegalArgumentException("해당 피드백을 찾을 수 없습니다: " + feedbackId));
    }

    // 한 유저의 정정 요청을 모두 가져옴
    // 클라이언트 사이드 필터링 -> 서버 사이드 필터링 수정 (현재 페이지 내에서만 필터링 되던 것을 DB 모든 자료 내에서 필터링해서 새로 pagination 되도록 수정)
    public Page<AdminFeedbackDto> getFeedbackByUserIdWithFilter(Integer userId, String startDateStr, String endDateStr, String statusStr, Pageable pageable) {
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        // 입력받은 startDate 를 LocalDateTime 으로 변환 (하루의 시작은 00:00:00)
        if (startDateStr != null && !startDateStr.isEmpty()) {
            // LocalDate 타입으로 받아와야 오류없이 yyyy-MM-dd 타입으로 저장 가능
            LocalDate localStart = LocalDate.parse(startDateStr);
            startDate = localStart.atStartOfDay();
        }

        // 마찬가지로 endDate를 LocalDateTime으로 변환 (하루의 끝은 23:59:59)
        if (endDateStr != null && !endDateStr.isEmpty()) {
            // LocalDate 타입으로 받아와야 오류없이 yyyy-MM-dd 타입으로 저장 가능
            LocalDate localEnd = LocalDate.parse(endDateStr);
            endDate = localEnd.atTime(23, 59, 59);
        }

        // status 변환 (String -> enum)
        AdminFeedback.FeedbackStatus status = null;
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                status = AdminFeedback.FeedbackStatus.valueOf(statusStr);
            } catch (IllegalArgumentException e) {
                System.out.println(">>> [AdminService] 잘못된 status 값: " + statusStr);
            }
        }

        Page<AdminFeedback> pagedFeedbacks = adminRepository.findFeedbacksByUserIdWithFilter(userId, startDate, endDate, status, pageable);
        return pagedFeedbacks.map(AdminFeedbackDto::fromEntity);
    }

    //댓글 저장
    @Transactional
    public AdminFeedbackCommentsDto saveComments(AdminFeedbackCommentsDto dto){
        //유저 찾기
        User user = userRepository.findByUserId(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자ID :"+dto.getUserId()));
        //피드백 찾기
        AdminFeedback feedback = adminRepository.findById(Long.valueOf(dto.getFeedbackId()))
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 피드백ID :"+dto.getFeedbackId()));

        //Dto -> Entity
        AdminFeedbackComments savedComment = commentsRepository.save(dto.toEntity(user, feedback));

        return AdminFeedbackCommentsDto.fromEntity(savedComment);
    }

    //특정 피드백 댓글 조회
    @Transactional
    public Optional<AdminFeedbackComments> getCommentByFeedbackId(Integer feedbackId) {
        return commentsRepository.findFirstByFeedback_FeedbackIdOrderByCreatedAtAsc(feedbackId);
    }
}