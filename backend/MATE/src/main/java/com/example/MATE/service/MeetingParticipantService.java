package com.example.MATE.service;

import com.example.MATE.model.MeetingParticipant;
import com.example.MATE.model.User;
import com.example.MATE.repository.MeetingParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingParticipantService {

    private final MeetingParticipantRepository meetingParticipantRepository;

    // findParticipantsByMeetingIdNative(meetingId) 메서드가 반환하는 데이터는 List<MeetingParticipant>
    // usersearchlog.html에서는 참여자(User) 정보가 필요하므로, 각 MeetingParticipant 객체에서 user 필드를 추출해야 함.
    List<User> getParticipantsByMeetingId(Integer meetingId) {
        return meetingParticipantRepository.findParticipantsByMeetingId(meetingId)
                                           .stream()
                                           .map(MeetingParticipant::getUser)
                                           .toList();
    }

//    @Transactional
    public void updateIsAttending(Integer meetingId, Integer userId) {
        MeetingParticipant participant = meetingParticipantRepository.findByMeeting_MeetingIdAndUser_UserId(meetingId, userId)
                .orElseThrow(() -> new IllegalArgumentException("참여자를 찾을 수 없습니다."));

        participant.setAttending(false); // is_attending을 false로 설정
        meetingParticipantRepository.save(participant); // 저장
    }


    public List<MeetingParticipant> getAttendingParticipantsByMeetingId(Integer meetingId) {
        return meetingParticipantRepository.findByMeeting_MeetingIdAndIsAttendingTrue(meetingId);
    }
}

