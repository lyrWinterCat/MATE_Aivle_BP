package com.example.MATE.controller;

import com.example.MATE.dto.MeetingDto;
import com.example.MATE.dto.MeetingParticipantDto;
import com.example.MATE.model.Meeting;
import com.example.MATE.model.MeetingParticipant;
import com.example.MATE.service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/meeting")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;
    
    //회의 데이터 생성
    @PostMapping("/create")
    public ResponseEntity<?> createMeeting(@RequestBody Map<String, Object> request){
        System.out.println("[MeetingController/createMeeting] 실행!");
        try {
            String meetingTitle = (String) request.get("meetingTitle");
            String meetingUrl = (String) request.get("meetingUrl");
            Integer userId = Integer.parseInt(request.get("userId").toString()); //형식문제조심

            MeetingDto meetingDto = new MeetingDto();
            meetingDto.setMeetingName(meetingTitle);
            meetingDto.setMeetingUrl(meetingUrl);
            MeetingDto savedMeeting = meetingService.createMeeting(meetingDto, userId);

            return ResponseEntity.ok().body(
                    //응답을 json으로 변환
                    Map.of("success",true,"meetingId",savedMeeting.getMeetingId())
            );
        }catch(Exception e){
            return ResponseEntity.status(500).body(
                    //응답을 json으로 변환
                    Map.of("success",false,"message","회의 생성 중 오류 발생 :"+e.getMessage())
            );
        }
    }

    @GetMapping("/host/{meetingId}")
    public String meetingRecorder(@PathVariable("meetingId") Integer meetingId, Model model) {
        Meeting meeting = meetingService.getMeetingByMeetingId(meetingId);

        // 날짜 포맷팅
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

        String formattedDate = meeting.getCreatedAt().format(dateFormatter);
        String formattedTime = meeting.getCreatedAt().format(timeFormatter);

        model.addAttribute("meetingParticipants", meeting.getMeetingParticipants());
        model.addAttribute("meetingName", meeting.getMeetingName());
        model.addAttribute("meetingDate", formattedDate);
        model.addAttribute("meetingTime", formattedTime);
        model.addAttribute("participantCount", meeting.getMeetingParticipants().size()); // 참여자 수 추가
        model.addAttribute("meetingId", meetingId);
        return "meeting/host";
    }


    @GetMapping("/{meetingId}/participants")
    public ResponseEntity<Map<String, Object>> getMeetingParticipants(@PathVariable Integer meetingId) {
        try {
            List<MeetingParticipant> participants = meetingService.getParticipantsByMeetingId(meetingId);
            List<MeetingParticipantDto> participantDtos = participants.stream()
                    .map(participant -> new MeetingParticipantDto(
                            participant.getParticipantId(),
                            participant.getUser().getName() // User의 name 필드
                    ))
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("participantCount", participantDtos.size());
            response.put("meetingParticipants", participantDtos);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "참여자 정보를 가져오는 중 오류 발생: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/client/{meetingId}")
    public String meetingUser(@PathVariable("meetingId") Integer meetingId, Model model){
        System.out.println("[MeetingController] 하이 클라이언트입니다.");
        //참여자와 createAt 가져오기? 여기도 필요한가
        Meeting meeting = meetingService.getMeetingByMeetingId(meetingId);
        model.addAttribute("meetingParticipants",meeting.getMeetingParticipants());
        model.addAttribute("meetingName",meeting.getMeetingName());
        model.addAttribute("meetingCreatedAt",meeting.getCreatedAt());
        System.out.println(">>> : "+meeting.getMeetingName());
        return "meeting/client";
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
