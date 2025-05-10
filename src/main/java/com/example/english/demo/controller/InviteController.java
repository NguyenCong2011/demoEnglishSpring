package com.example.english.demo.controller;

import com.example.english.demo.entity.CompetitionResult;
import com.example.english.demo.entity.Invitation;
import com.example.english.demo.entity.InviteMessage;
import com.example.english.demo.entity.ToeicExam;
import com.example.english.demo.entity.User;
import com.example.english.demo.repository.CompetitionResultRepository;
import com.example.english.demo.repository.ToeicExamRepository;
import com.example.english.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Controller
public class InviteController {

    private final SimpMessagingTemplate messagingTemplate;
    private final CompetitionResultRepository competitionResultRepository;
    private final UserRepository userRepository;
    private final ToeicExamRepository toeicExamRepository;

    @MessageMapping("/invite")
    public void sendInvite(@Payload InviteMessage message) {
        log.info("Received invite request from {} to {}", message.getSenderId(), message.getReceiverId());

        log.info("Received accept invite from {} to {} in room {}",
                message.getReceiverId(), message.getSenderId(), message.getRoomId());

        String destination = "/topic/invite/" + message.getReceiverId();
        log.info("Sending invite to destination: {}", destination);

        messagingTemplate.convertAndSend(destination, message);
    }

    @MessageMapping("/accept-invite")
    public void handleAcceptInvite(Invitation invite) {
        log.info("Received accept invite from {} to {}", invite.getReceiverId(), invite.getSenderId());

        // Notify both users about the acceptance
        messagingTemplate.convertAndSend("/topic/accept/" + invite.getSenderId(), invite);
        messagingTemplate.convertAndSend("/topic/accept/" + invite.getReceiverId(), invite);

    }

    @MessageMapping("/reject-invite")
    public void handleRejectInvite(Invitation invite) {
        log.info("Received reject invite from {} to {}", invite.getReceiverId(), invite.getSenderId());
        messagingTemplate.convertAndSend("/topic/reject/" + invite.getSenderId(), invite);
    }
}
