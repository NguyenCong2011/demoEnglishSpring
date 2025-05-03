package com.example.english.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InviteMessage {
    private String senderId;
    private String senderUsername;
    private String receiverId;
    private Long examId;
    private String examName;
}
