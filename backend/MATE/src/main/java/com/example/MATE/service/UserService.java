package com.example.MATE.service;

import com.example.MATE.dto.MeetingLogDto;
import com.example.MATE.model.Meeting;
import com.example.MATE.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final MeetingService meetingService;
    private final MeetingParticipantService meetingParticipantService;

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
