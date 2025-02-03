package com.example.MATE.service;

import com.example.MATE.dto.MeetingDto;
import com.example.MATE.model.Meeting;
import com.example.MATE.model.MeetingParticipant;
import com.example.MATE.model.ScreenData;
import com.example.MATE.model.User;
import com.example.MATE.repository.MeetingParticipantRepository;
import com.example.MATE.repository.MeetingRepository;
import com.example.MATE.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;

    public List<Meeting> getAllMeetings() {
        return meetingRepository.findAllMeetings();
    }

    // 유저 ID 를 받아 해당 유저가 참여한 모든 미팅 ID 를 반환
    public List<Integer> getMeetingIdsByUserId(Integer userId) {
        return meetingRepository.findMeetingIdsByUserId(userId);
    }

    // 유저 Id를 받아 해당 유저가 참여한 모든 Meeting 객체를 바로 반환 (
    public Page<Meeting> getMeetingsByUserIdWithPaging(Integer userId, Pageable pageable) {
        return meetingRepository.findByMeetingParticipants_User_UserId(userId, pageable);
    }

    // 미팅 ID 를 받아 해당 미팅의 정보를 반환
    public Meeting getMeetingByMeetingId(Integer meetingId) {
        return meetingRepository.findByMeetingId(meetingId);
    }

    //새로운 회의생성 + 회의참여자 추가
    @Transactional
    public MeetingDto createMeeting(MeetingDto meetingDto, Integer userId) {
        System.out.println("[MeetingService/createMeeting] 실행!");
        //url 중복여부확인
        if( meetingRepository.existsByUrl(meetingDto.getMeetingUrl()) ){
            throw new IllegalArgumentException("이미 존재하는 회의 URL입니다. URL을 다시 확인해주세요.");
        }

        //Dto-> entity 변환 후 DB 저장
        Meeting meeting = meetingDto.toEntity();
        meeting.setStartTime(LocalDateTime.now());
        meeting.setCreatedAt(LocalDateTime.now());
        meeting.setEndTime(null);
        meeting.setLastBreakTime(null);
        meeting.setFilepath("");

        //저장
        Meeting saveMeting = meetingRepository.save(meeting);

        if(saveMeting.getMeetingId() == null){
            throw new IllegalArgumentException("회의가 정상적으로 저장되지 않음");
        }
        //사용자 조회
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자("+userId+")는 존재하지 않습니다."));

        //MeetingParticipant에 저장
        MeetingParticipant participant = new MeetingParticipant();
        participant.setMeeting(saveMeting);
        participant.setUser(user);
        participant.setRecording(true);

        meetingParticipantRepository.save(participant);
        System.out.println("[MeetingService/createMeeting] 회의 및 참가자 저장 완료");
        //entity->Dto 반환
        return MeetingDto.fromEntity(saveMeting);
    }
}
