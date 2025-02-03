package com.example.MATE.service;

import com.example.MATE.dto.AdminFeedbackDto;
import com.example.MATE.dto.MeetingLogDto;
import com.example.MATE.dto.SpeechLogDto;
import com.example.MATE.model.*;
import com.example.MATE.repository.AdminRepository;
import com.example.MATE.repository.MeetingRepository;
import com.example.MATE.repository.ToxicityLogRepository;
import com.example.MATE.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.jdbc.Expectation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final MeetingRepository meetingRepository;


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


    // Pagination : 현재 로그인한 유저가 참여한 미팅들의 미팅명, 시작시간, 참여자 목록 반환
    public Page<MeetingLogDto> getMeetingLogsWithPaging(Integer userId, Pageable pageable) {
        //최신순 정렬
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC,"createdAt")
        );
        Page<Meeting> pagedMeetings = meetingService.getMeetingsByUserIdWithPaging(userId, sortedPageable); // 현재 유저가 참여한 모든 회의 반환
        return pagedMeetings.map(meeting -> {
            List<User> participants = meetingParticipantService.getParticipantsByMeetingId(meeting.getMeetingId()); // 한 회의의 참가자들을 모두 가져옴
            return MeetingLogDto.fromEntity(meeting, participants); // Meeting 객체 하나하나를 MeetingDto 객체로 변환 (이름 ,로 연결 / 날짜 포맷팅 등이 처리됨)
        });
    }

    // Pagination : 현재 로그인한 유저가 참여한 미팅들의 미팅명, 시작시간, 참여자 목록 반환
    public Page<MeetingLogDto> getMeetingLogsSSF(Integer userId, String employeeName, String startDate, String endDate, Pageable pageable) {
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;

        if (startDate != null && !startDate.isEmpty()) {
            startDateTime = LocalDate.parse(startDate).atStartOfDay();
        }

        if (endDate != null && !endDate.isEmpty()) {
            endDateTime = LocalDate.parse(endDate).atTime(23, 59, 59);
        }

        Page<Meeting> meetings = meetingRepository.findMeetingsByUserIdSSF(
                userId,
                employeeName != null ? employeeName : "",
                startDateTime,
                endDateTime,
                pageable
        );

        return meetings.map(meeting -> {
            List<User> participants = meeting.getMeetingParticipants().stream()
                    .map(MeetingParticipant::getUser)
                    .toList();
            return MeetingLogDto.fromEntity(meeting, participants);
        });
    }

    public Page<SpeechLogDto> getSpeechLogsByUserId(Integer userId, Pageable pageable) {
        //최신순정렬
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC,"timestamp")
        );
        Page<SpeechLog> speechLogs = userRepository.findSpeechLogsByUserId(userId, sortedPageable);

        return speechLogs.map(this::convertToSpeechLogDto);
    }

    // 모든 발화로그를 가져와서 Page 에 집어넣음
    public Page<SpeechLogDto> getPagedSpeechLogs(Pageable pageable) {
        Page<SpeechLog> pagedSpeechLogs =  userRepository.findAllSpeechLogsWithPaging(pageable);

        return pagedSpeechLogs.map(this::convertToSpeechLogDto); // 포맷팅, 독성 or 일반 표시 등의 처리를 해주기 위해 Dto 로 변환
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