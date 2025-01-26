package com.example.MATE.service;

import com.example.MATE.dto.MeetingLogDto;
import com.example.MATE.dto.SpeechLogDto;
import com.example.MATE.model.Meeting;
import com.example.MATE.model.SpeechLog;
import com.example.MATE.model.User;
import com.example.MATE.repository.ToxicityLogRepository;
import com.example.MATE.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final MeetingService meetingService;
    private final MeetingParticipantService meetingParticipantService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ToxicityLogRepository toxicityLogRepository;

    //회원조회
    public Optional<User> findByEmail(String email){
        System.out.println(">>> [UserService] 회원 조회 : "+email);
        return userRepository.findByEmail(email);
    }

    //비밀번호 검증 - 비밀번호 변경 시 필요 로직
    public boolean checkPassword(String email, String rawPassword){
        //사용자가 새로 입력한 비밀번호와 현재 DB에 저장된 비밀번호 일치 여부
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    // 현재 로그인한 유저가 참여한 미팅들의 미팅명, 시작시간, 참여자 목록을 담은 List 를 반환
    // user/meetingList 페이지에서 사용
    public List<MeetingLogDto> getMeetingLogs(Integer userId) {
        List<Integer> meetingIds = meetingService.getMeetingIdsByUserId(userId); // 현재 로그인한 유저가 참여한 미팅 ID 목록
//        List<MeetingLogDto> meetingLogs = new ArrayList<>(); // 미팅명, 시작시간, 참여자 목록을 담을 List
//
//        // 미팅 ID 목록을 순회하며 각 미팅의 정보를 추출, 그 후 meetingLogs 에 추가
//        for (Integer meetingId : meetingIds) {
//            Meeting meeting = meetingService.getMeetingByMeetingId(meetingId);
//            List<User> participants = meetingParticipantService.getParticipantsByMeetingId(meetingId);
//            String participantNames = participants.stream()
//                                                  .map(User::getName)
//                                                  .collect(Collectors.joining(", "));
//
//            meetingLogs.add(new MeetingLogDto(
//                    meeting.getMeetingName(),
//                    meeting.getStartTime().toString(),
//                    participantNames
//            ));
//        }
//
//        return meetingLogs;

        return meetingIds.stream().map(meetingId -> {
            Meeting meeting = meetingService.getMeetingByMeetingId(meetingId); // 하나의 회의 객체를 받아옴
            List<User> participants = meetingParticipantService.getParticipantsByMeetingId(meetingId); // 하나의 회의에 참여한 사람들을 받아옴
            return MeetingLogDto.fromEntity(meeting, participants); // 회의 정보와 참여자 정보를 이용해 MeetingLogDto 객체 생성
        }).collect(Collectors.toList()); // 회의 정보를 담은 MeetingLogDto 객체들을 List 로 반환
    }

    public List<SpeechLogDto> getSpeechLogsByUserId(Integer userId) {
        List<SpeechLog> speechLogs = userRepository.findSpeechLogsByUserId(userId);

        return speechLogs.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<SpeechLogDto> getAllSpeechLogs() {
        List<SpeechLog> speechLogs = userRepository.findAllSpeechLogs();

        return speechLogs.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // SpeechLog 객체를 받아서 SpeechLogDto 객체로 변환
    public SpeechLogDto convertToDto(SpeechLog speechLog) {
        SpeechLogDto speechLogDto = new SpeechLogDto();

        // 날짜 포맷 정의 (yyyy-MM-dd HH:mm:ss)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 발화시간, 발화내용, 발화자를 가져올거임
        speechLogDto.setTimestamp(speechLog.getTimestamp().format(formatter));
        speechLogDto.setContent(speechLog.getContent());
        speechLogDto.setUserName(speechLog.getUser().getName());

        // 독성인지 아닌지 판단
        // toxicity_log 테이블에 있으면 독성, 없으면 일반
        boolean isToxic = toxicityLogRepository.existsBySpeechLog_LogId(speechLog.getLogId());
        speechLogDto.setSpeechType(isToxic ? "독성" : "일반");

        return speechLogDto;

    }
}