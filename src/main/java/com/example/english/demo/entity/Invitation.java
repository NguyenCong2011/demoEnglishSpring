package com.example.english.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invitation {
    private String senderId;
    private String senderUsername;
    private String receiverId;
    private String examId;
    private String examName;
}
