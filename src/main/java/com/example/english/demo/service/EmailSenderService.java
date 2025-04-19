package com.example.english.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
@RequiredArgsConstructor
@Service
public class EmailSenderService {

    private final JavaMailSender javaMailSender;

    public void sendMail(SimpleMailMessage mailMessage){
        javaMailSender.send(mailMessage);
    }
}
