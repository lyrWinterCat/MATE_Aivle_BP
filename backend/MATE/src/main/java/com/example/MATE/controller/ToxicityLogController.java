package com.example.MATE.controller;

import com.example.MATE.dto.ToxicityLogDto;
import com.example.MATE.service.ToxicityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/toxic")
@RequiredArgsConstructor
public class ToxicityLogController {

    private final ToxicityLogService toxicityLogService;

    //유저 정정게시판에서 사용
    //특정 미팅에서 특정 사용자의 독성 로그 조회
    @PostMapping("/userToxicLog")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<List<ToxicityLogDto>> getToxicityLogsByMeetingAndUser(@RequestBody Map<String, Integer> request){
        System.out.println(">>> [ToxicityLogController/getToxicityLogsByMeetingAndUser] request : "+ request);

        Integer meetingId = request.get("meetingId");
        Integer userId = request.get("userId");

        if (meetingId == null || userId == null) {
            return ResponseEntity.badRequest().build(); // meetingId 또는 userId 누락 시 400 응답
        }

        List<ToxicityLogDto> toxicityLogs = toxicityLogService.getToxicityLogsByMeetingAndUser(meetingId, userId);
        return ResponseEntity.ok(toxicityLogs);
    }
}
