package com.example.MATE.service;

import com.example.MATE.dto.AdminFeedbackDto;
import com.example.MATE.dto.RequestDto;
import com.example.MATE.model.AdminFeedback;
import com.example.MATE.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;

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

    public AdminFeedback getFeedbackById(Integer feedbackId) {
        return adminRepository.findById(Long.valueOf(feedbackId))
                .orElseThrow(() -> new IllegalArgumentException("해당 피드백을 찾을 수 없습니다: " + feedbackId));
    }

    // 한 유저의 정정 요청을 모두 가져옴
    public Page<AdminFeedbackDto> getFeedbackByUserId(Integer userId, Pageable pageable) {
        //최신순 조회
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("createdAt").descending());
        return adminRepository.findByUser_UserId(userId, sortedPageable)
                .map(AdminFeedbackDto::fromEntity);
    }
}