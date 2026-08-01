package com.example.english.demo.controller;

import com.example.english.demo.dto.request.LoginRequest;
import com.example.english.demo.repository.UserRepository;
import com.example.english.demo.service.AuthenticationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@Controller
@CrossOrigin(origins = "http://localhost:4200")
public class HomeController {
    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;


    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("name", "John");
        return "home";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String authenticate(LoginRequest request,
                            HttpServletResponse response,
                            Model model) {

        var result = authenticationService.authenticate(request);

        if (result.isAuthenticated()) {
            Cookie cookie = new Cookie("jwt", result.getToken());
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(7 * 24 * 60 * 60);
            response.addCookie(cookie);

            return "redirect:/";
        }

        model.addAttribute("error", "Invalid credentials");
        return "login";
    }


    @RequestMapping("/about")
    public String about(Model model) {
        return "about";
    }

    @RequestMapping("/online-tests")
    public String onlineTests(Model model) {
        return "online-tests";
    }


}
