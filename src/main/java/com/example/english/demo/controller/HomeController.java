package com.example.english.demo.controller;

import com.example.english.demo.dto.request.ApiResponse;
import com.example.english.demo.dto.request.AuthenticationRequest;
import com.example.english.demo.dto.request.LogoutRequest;
import com.example.english.demo.dto.response.AuthenticationResponse;
import com.example.english.demo.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RequiredArgsConstructor
@Controller
public class HomeController {
    private final AuthenticationService authenticationService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("name", "John");
        return "home";
    }

    @GetMapping("/login")
    public String login(Model model) {
        return "login";
    }

//    @PostMapping("/login")
//    public String authenticate(@ModelAttribute @Valid AuthenticationRequest request, Model model) {
//        ApiResponse<AuthenticationResponse> apiResponse = new ApiResponse<>();
//
//        try {
//            var result = authenticationService.authenticate(request);
//
//            if (result.isAuthenticated()) {
//                apiResponse.setResult(result);
//                return "redirect:/";
//            } else {
//                model.addAttribute("error", "Authentication failed");
//                return "login";
//            }
//        } catch (Exception e) {
//            model.addAttribute("error", "Invalid username or password!");
//            return "login";
//        }
//    }

    @PostMapping("/login")
    public String authenticate(@ModelAttribute @Valid AuthenticationRequest request,
                               HttpServletResponse response,
                               Model model) {
        try {
            var result = authenticationService.authenticate(request);

            if (result.isAuthenticated()) {
                // 👇 Đặt JWT vào cookie
                Cookie cookie = new Cookie("jwt", result.getToken());
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                cookie.setMaxAge(7 * 24 * 60 * 60); // 7 ngày
                response.addCookie(cookie);

                return "redirect:/";
            } else {
                model.addAttribute("error", "Authentication failed");
                return "login";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Invalid username or password!");
            return "login";
        }
    }

    @RequestMapping("/about")
    public String about(Model model) {
        return "about";
    }

    @RequestMapping("/online-tests")
    public String onlineTests(Model model) {
        return "online-tests";
    }

    @GetMapping("/toeic")
    public String showToeicPage() {
        return "toeic";
    }

}
