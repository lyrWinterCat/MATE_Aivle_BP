package com.example.screenshare.service;

import com.example.screenshare.model.ScreenData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ScreenShareService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void broadcastScreenData(ScreenData screenData) {
        messagingTemplate.convertAndSend("/topic/public", screenData);
    }
}
