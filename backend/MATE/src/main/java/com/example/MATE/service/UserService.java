package com.example.MATE.service;

import com.example.MATE.dto.MeetingLogDto;
import com.example.MATE.dto.UserDto;
import com.example.MATE.model.Meeting;
import com.example.MATE.model.User;
import com.example.MATE.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseStatus;

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

    //회원조회
    public Optional<User> findByEmail(String email){
        System.out.println(">>> 회원 조회[UserService]");
        return userRepository.findByEmail(email);
    }

    //비밀번호 검증 - 비밀번호 변경 시 필요 로직
    public boolean checkPassword(String email, String rawPassword){
        //사용자가 새로 입력한 비밀번호와 현재 DB에 저장된 비밀번호 일치 여부
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    //??
    public List<MeetingLogDto> getMeeetingLogs() {
        List<Meeting> meetings = meetingService.getAllMeetings();
        List<MeetingLogDto> meetingLogs = new ArrayList<>();

        for (Meeting meeting : meetings) {
            List<User> participants = meetingParticipantService.getParticipantsByMeetingId(meeting.getMeetingId());
            String participantNames = participants.stream()
                                                  .map(User::getName)
                                                  .collect(Collectors.joining(", "));

            meetingLogs.add(new MeetingLogDto(
                    meeting.getMeetingName(),
                    meeting.getStartTime().toString(),
                    participantNames
            ));
        }

        return meetingLogs;
    }
}
