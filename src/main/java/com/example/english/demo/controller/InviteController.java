package com.example.english.demo.controller;

import com.example.english.demo.entity.Invitation;
import com.example.english.demo.entity.InviteMessage;
import com.example.english.demo.service.CompetitionResultService;
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
    private final CompetitionResultService competitionResultService;

    @MessageMapping("/invite")
    public void sendInvite(@Payload InviteMessage message) {
        log.info("Received invite request from {} to {}", message.getSenderId(), message.getReceiverId());

        // ✅ Tạo roomId duy nhất
        String roomId = UUID.randomUUID().toString();
        message.setRoomId(roomId); // Gán roomId vào message để cả 2 phía dùng chung

        log.info("Received accept invite from {} to {} in room {}",
                message.getReceiverId(), message.getSenderId(), message.getRoomId());

        String destination = "/topic/invite/" + message.getReceiverId();
        log.info("Sending invite to destination: {}", destination);

        // ✅ Gửi kèm roomId
        messagingTemplate.convertAndSend(destination, message);
    }

    @MessageMapping("/accept-invite")
    public void handleAcceptInvite(Invitation invite) {
        log.info("Received accept invite from {} to {}", invite.getReceiverId(), invite.getSenderId());

        // Notify both users about the acceptance
        messagingTemplate.convertAndSend("/topic/accept/" + invite.getSenderId(), invite);
        messagingTemplate.convertAndSend("/topic/accept/" + invite.getReceiverId(), invite);

        // Notify both users about the competition room
        messagingTemplate.convertAndSend("/topic/competition/" + invite.getRoomId(), 
            Map.of("status", "started", "roomId", invite.getRoomId()));
    }

    @MessageMapping("/reject-invite")
    public void handleRejectInvite(Invitation invite) {
        log.info("Received reject invite from {} to {}", invite.getReceiverId(), invite.getSenderId());
        messagingTemplate.convertAndSend("/topic/reject/" + invite.getSenderId(), invite);
    }
}
