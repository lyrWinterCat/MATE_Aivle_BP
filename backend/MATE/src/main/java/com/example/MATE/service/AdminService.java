package com.example.MATE.service;

import com.example.MATE.dto.RequestDto;
import org.apache.coyote.Request;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {
    public List<RequestDto> getAllRequests() {
        // 모든 요청 가져오기 로직
        return new ArrayList<>();
    }

//    Request request = requestRepository.findById(requestDto.getId())
//            .orElseThrow(() -> new RuntimeException("Request not found"));
//    // 요청 응답 처리 로직 추가
//        request.setDescription(requestDto.getDescription());
//        requestRepository.save(request);
}
