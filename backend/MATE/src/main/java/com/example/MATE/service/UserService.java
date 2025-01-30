package com.example.MATE.service;

import com.example.MATE.dto.AdminFeedbackDto;
import com.example.MATE.dto.MeetingLogDto;
import com.example.MATE.dto.SpeechLogDto;
import com.example.MATE.model.*;
import com.example.MATE.repository.AdminRepository;
import com.example.MATE.repository.ToxicityLogRepository;
import com.example.MATE.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.jdbc.Expectation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
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
    private final AdminRepository adminRepository;


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

        return meetingIds.stream().map(meetingId -> {
            Meeting meeting = meetingService.getMeetingByMeetingId(meetingId); // 하나의 회의 객체를 받아옴
            List<User> participants = meetingParticipantService.getParticipantsByMeetingId(meetingId); // 하나의 회의에 참여한 사람들을 받아옴
            return MeetingLogDto.fromEntity(meeting, participants); // 회의 정보와 참여자 정보를 이용해 MeetingLogDto 객체 생성
        }).collect(Collectors.toList()); // 회의 정보를 담은 MeetingLogDto 객체들을 List 로 반환
    }

    public Page<MeetingLogDto> getMeetingLogsWithPaging(Integer userId, Pageable pageable) {
        // 구현 필요
        Page<Meeting> pagedMeetings = meetingService.getMeetingsByUserIdWithPaging(userId, pageable);
        return pagedMeetings.map(meeting -> {
            List<User> participants = meetingParticipantService.getParticipantsByMeetingId(meeting.getMeetingId());
            return MeetingLogDto.fromEntity(meeting, participants);
        });
    }

    public Page<SpeechLogDto> getSpeechLogsByUserId(Integer userId, Pageable pageable) {
        Page<SpeechLog> speechLogs = userRepository.findSpeechLogsByUserId(userId, pageable);

        return speechLogs.map(this::convertToSpeechLogDto);
    }

    public List<SpeechLogDto> getAllSpeechLogs() {
        List<SpeechLog> speechLogs = userRepository.findAllSpeechLogs();

        return speechLogs.stream()
                .map(this::convertToSpeechLogDto)
                .collect(Collectors.toList());
    }

    public Page<SpeechLogDto> getPagedSpeechLogs(Pageable pageable) {
        Page<SpeechLog> pagedSpeechLogs =  userRepository.findAllSpeechLogsWithPaging(pageable);

        return pagedSpeechLogs.map(this::convertToSpeechLogDto);
    }

    // SpeechLog 객체를 받아서 SpeechLogDto 객체로 변환
    public SpeechLogDto convertToSpeechLogDto(SpeechLog speechLog) {
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

    //정정게시글 등록
    @Transactional
    public AdminFeedbackDto writeFeedBack(AdminFeedbackDto adminFeedbackDto) {
        //요청값 검증
        if(adminFeedbackDto.getUserId()== null){
            throw new IllegalArgumentException("userID가 없음");
        }
        if(adminFeedbackDto.getToxicityId()==null){
            throw new IllegalArgumentException("toxicityId가 없음");
        }
        //현재 사용자 조회
        User user = userRepository.findByUserId(adminFeedbackDto.getUserId())
                .orElseThrow(()-> new IllegalArgumentException("유효하지 않은 사용자 ID : "+adminFeedbackDto.getUserId()));
        //독성 로그 조회
        ToxicityLog toxicityLog = toxicityLogRepository.findById(adminFeedbackDto.getToxicityId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 독성 ID: " + adminFeedbackDto.getToxicityId()));
        //Dto -> entity
        AdminFeedback adminFeedback = adminFeedbackDto.toEntity(user, toxicityLog);
        AdminFeedback saveFeedback = adminRepository.save(adminFeedback);

        return AdminFeedbackDto.fromEntity(saveFeedback);
    }

    //정정게시글 상태 수정
    @Transactional
    public void updateFeedback(AdminFeedback feedback) {
        adminRepository.save(feedback);
    }
}