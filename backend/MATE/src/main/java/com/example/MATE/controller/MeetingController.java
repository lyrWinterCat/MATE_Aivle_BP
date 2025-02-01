package com.example.MATE.controller;

import com.example.MATE.dto.MeetingDto;
import com.example.MATE.model.ScreenData;
import com.example.MATE.service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/meeting")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    @GetMapping("/host")
    public String meetingRecorder(){
        return "meeting/host";
    }

    @GetMapping("/client")
    public String meetingUser(){
        return "meeting/client";
    }


    @GetMapping("/view")
    public String meetingView(){
        return "meeting/view";
    }

    @MessageMapping("/signal")
    @SendTo("/topic/public")
    public ScreenData handleSignal(ScreenData signal) {
        return signal;
    }

    @MessageMapping("/ice-candidate")
    @SendTo("/topic/public")
    public ScreenData handleIceCandidate(ScreenData candidate) {
        return candidate;
    }

    //유저의 모든 미팅 목록 조회(번호, 미팅이름)
    @PostMapping("/user/meetingInfo")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<List<MeetingDto>> getMeetingInfoByUserId(@RequestBody Map<String, Object> request){
        System.out.println(">>> [MeetingController/getMeetingInfoByUserId] request : "+ request);
        if(!request.containsKey("userId")){
            throw new IllegalArgumentException("userId가 요청에 없음");
        }
        Integer userId;
        try {
            userId = Integer.parseInt(request.get("userId").toString());
        }catch (NumberFormatException e){
            throw new IllegalArgumentException("userId는 숫자임.");
        }

        List<Integer> meetingIds = meetingService.getMeetingIdsByUserId(userId);

        List<MeetingDto> meetingInfoList = meetingIds.stream()
                .map(meetingService::getMeetingByMeetingId)
                .map(MeetingDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(meetingInfoList);
    }
}
