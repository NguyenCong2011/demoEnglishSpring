package com.example.english.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/toeic")
public class ToeicController {
    
    @GetMapping
    public String getToeicPage() {
        return "toeic-user";
    }
} 