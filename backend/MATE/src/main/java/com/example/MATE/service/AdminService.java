package com.example.MATE.service;

import com.example.MATE.dto.RequestDto;
import com.example.MATE.model.AdminFeedback;
import com.example.MATE.repository.AdminRepository;
import org.apache.coyote.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    public List<RequestDto> getAllRequests() {
        // 모든 요청 가져오기 로직
        return new ArrayList<>();
    }

    public List<AdminFeedback> getFeedbackList() {
        // 모든 피드백 가져오기 로직
        return adminRepository.findFeedbacks();
    }
}