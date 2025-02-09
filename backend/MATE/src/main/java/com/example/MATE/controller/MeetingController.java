package com.example.MATE.controller;

import com.example.MATE.dto.MeetingDetailDto;
import com.example.MATE.dto.MeetingDto;
import com.example.MATE.dto.MeetingParticipantDto;
import com.example.MATE.dto.SummaryDto;
import com.example.MATE.model.Meeting;
import com.example.MATE.model.MeetingParticipant;
import com.example.MATE.model.Summary;
import com.example.MATE.service.MeetingParticipantService;
import com.example.MATE.service.MeetingService;
import com.example.MATE.service.SummaryService;
import com.example.MATE.service.UserService;
import com.example.MATE.utils.DateUtil;
import com.example.MATE.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/meeting")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;
    private final UserService userService;
    private final MeetingParticipantService meetingParticipantService;
    private final SummaryService summaryService;
    
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
        model.addAttribute("meetingParticipants", meeting.getMeetingParticipants());
        model.addAttribute("meetingName", meeting.getMeetingName());
        model.addAttribute("meetingDate", DateUtil.dateFormat(meeting.getCreatedAt()));
        model.addAttribute("meetingTime", DateUtil.timeFormat(meeting.getCreatedAt()));
        model.addAttribute("participantCount", meeting.getMeetingParticipants().size()); // 참여자 수 추가
        model.addAttribute("meetingId", meetingId);
        return "meeting/host";
    }


    @GetMapping("/{meetingId}/participants")
    public ResponseEntity<Map<String, Object>> getMeetingParticipants(@PathVariable Integer meetingId) {
        try {
//            List<MeetingParticipant> participants = meetingService.getParticipantsByMeetingId(meetingId);
            List<MeetingParticipant> participants = meetingParticipantService.getAttendingParticipantsByMeetingId(meetingId);

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
        Meeting meeting = meetingService.getMeetingByMeetingId(meetingId);
        model.addAttribute("meetingId",meeting.getMeetingId());
        model.addAttribute("meetingParticipants", meeting.getMeetingParticipants());
        model.addAttribute("meetingName", meeting.getMeetingName());
        model.addAttribute("meetingDate", DateUtil.dateFormat(meeting.getCreatedAt()));
        model.addAttribute("meetingTime", DateUtil.timeFormat(meeting.getCreatedAt()));
        model.addAttribute("participantCount", meeting.getMeetingParticipants().size()); // 참여자 수 추가

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

    //(종료된 회의 포함) 유저의 모든 미팅 목록 조회(번호, 미팅이름)
    @PostMapping("/user/meetingInfoAll")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<List<MeetingDto>> getMeetingInfoByUserIdIncludingDone(@RequestBody Map<String, Object> request){
        System.out.println(">>> [MeetingController/getMeetingInfoByUserIdInCludingDone] request : "+ request);
        if(!request.containsKey("userId")){
            throw new IllegalArgumentException("userId가 요청에 없음");
        }
        Integer userId;
        try {
            userId = Integer.parseInt(request.get("userId").toString());
        }catch (NumberFormatException e){
            throw new IllegalArgumentException("userId는 숫자임.");
        }

        List<Integer> meetingIds = meetingService.getMeetingIdsByUserIdIncludingDone(userId);

        List<MeetingDto> meetingInfoList = meetingIds.stream()
                .map(meetingService::getMeetingByMeetingId)
                .map(MeetingDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(meetingInfoList);
    }

    @PostMapping("/checkMeetingUrl")
    public ResponseEntity<?> checkUrl(@RequestBody Map<String, String> request, Model model){
        System.out.println("[MeetingController/checkUrl] 실행!");

        String meetingUrl = request.get("meetingUrl");

        if (meetingUrl == null || meetingUrl.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("회의 URL을 입력해주세요.");
        }

        String meetingName = meetingService.getMeetingNameByUrl(meetingUrl);
        System.out.println("[meetingName] : "+meetingName);
        Map<String, String> response = new HashMap<>();

        if( meetingName == "" ){
            return ResponseEntity.ok("사용 가능한 회의 URL입니다. 회의 제목을 입력하고 접속을 클릭하세요.");
        }else{
            response.put("message", "이미 저장된 회의 URL입니다. 처음 참가하시는 회의라면 접속을 클릭하세요.");
            response.put("meetingName", meetingName); // 회의 이름 추가
            return ResponseEntity.status(409).body(response); // JSON 형태로 반환
        }
    }

    @PostMapping("/{meetingId}/start")
    public ResponseEntity<?> startMeeting(@PathVariable Integer meetingId) {
        meetingService.startMeeting(meetingId);
        return ResponseEntity.ok().body("회의 시작 시간이 기록되었습니다.");
    }

    @PostMapping("/{meetingId}/break")
    public ResponseEntity<?> takeBreak(@PathVariable Integer meetingId) {
        meetingService.takeBreak(meetingId);
        return ResponseEntity.ok().body("휴식 시간이 기록되었습니다.");
    }

    @PostMapping("/{meetingId}/end")
    public ResponseEntity<?> endMeeting(@PathVariable Integer meetingId) {
        String email = SecurityUtils.getCurrentUserEmail();
        Integer userId = userService.findUserByEmail(email);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("사용자가 인증되지 않았습니다.");
        }

        meetingParticipantService.updateIsAttending(meetingId, userId);
        meetingService.endMeeting(meetingId);
        return ResponseEntity.ok().body("회의 종료 시간이 기록되었습니다.");
    }

    // host에서 회의 종료 시 user/userMain으로 가기 위한 도메인 받아오기
    // client에서도 이거 받아다 쓰시면 됩니다
    @Value("${server.domain}")
    private String domain;

    @GetMapping("/domain")
    public ResponseEntity<String> getDomain() {
        return ResponseEntity.ok(domain);
    }

    //client  사용 - 참여자정보, 시작시간 가져오기
    @GetMapping("/client/{meetingId}/participants")
    public ResponseEntity<Map<String, Object>> getMeetingParticipantsClient(@PathVariable Integer meetingId) {
        try {
            List<MeetingParticipant> participants = meetingService.getParticipantsByMeetingId(meetingId);
            //회의참여자 테이블 참고
            List<MeetingParticipantDto> participantDtos = participants.stream()
                    .map(participant -> new MeetingParticipantDto(
                            participant.getParticipantId(),
                            participant.getUser().getName() // User의 name 필드
                    ))
                    .collect(Collectors.toList());
            //회의테이블 참고 -시작시간
            Meeting meeting = meetingService.getMeetingByMeetingId(meetingId);
            System.out.println(">>meetingController>>>"+meeting.getStartTime());

            Map<String, Object> response = new HashMap<>();
            response.put("participantCount", participantDtos.size());
            response.put("meetingParticipants", participantDtos);
            response.put("meetingStartTime", meeting.getStartTime());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "참여자 정보를 가져오는 중 오류 발생: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    //client  사용 - 요약 가져오기
    @PostMapping("/client/{meetingId}/summary")
    public ResponseEntity<?> getSummaryClient(@PathVariable Integer meetingId) {
        System.out.println(">>> [MeetingController / getSummaryClient] meetingId: " + meetingId);

        try {
            ResponseEntity<?> summaryDto = summaryService.getSummaryByMeetingId(meetingId);
            return ResponseEntity.ok(summaryDto);
        } catch (RuntimeException e) {
            System.out.println(">>> [MeetingController / getSummaryClient] 요약 데이터 없음: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("요약 데이터가 없습니다.");
        }
    }
    //client  사용 - 자료화면 요약 가져오기
    @PostMapping("/client/{meetingId}/imagesummary")
    public ResponseEntity<?> getImageSummaryClient(@PathVariable Integer meetingId) {
        System.out.println(">>> [MeetingController / getImageSummaryClient] meetingId: " + meetingId);
        try {
            ResponseEntity<?> summaryDto = summaryService.getSummaryByMeetingId(meetingId);
            return ResponseEntity.ok(summaryDto);
        } catch (RuntimeException e) {
            System.out.println(">>> [MeetingController / getImageSummaryClient] 요약 데이터 없음: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("요약 데이터가 없습니다.");
        }
    }
    //client  사용 - 종료시간 가져오기
    @PostMapping("/client/{meetingId}/endTime")
    public ResponseEntity<Map<String, Object>> getEndTime(@PathVariable Integer meetingId) {
        System.out.println(">>> [MeetingController / getEndTime] meetingId: " + meetingId);

        try {
            Meeting meeting = meetingService.getMeetingByMeetingId(meetingId);
            Map<String, Object> response = new HashMap<>();
            response.put("meetingEndTime", meeting.getEndTime());
            System.out.println(">>> meetingEndTime : "+meeting.getEndTime());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "종료시간을 가져오는 중 오류 발생: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
