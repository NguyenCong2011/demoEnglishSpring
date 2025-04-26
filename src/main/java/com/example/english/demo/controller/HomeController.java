package com.example.english.demo.controller;

import com.example.english.demo.dto.request.AuthenticationRequest;
import com.example.english.demo.entity.User;
import com.example.english.demo.exception.AppException;
import com.example.english.demo.exception.ErrorCode;
import com.example.english.demo.repository.UserRepository;
import com.example.english.demo.service.AuthenticationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
public class HomeController {
    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;


    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("name", "John");
        return "home";
    }

    @GetMapping("/login")
    public String login(Model model) {
        return "login";
    }


    @PostMapping("/login")
    public String authenticate(@ModelAttribute @Valid AuthenticationRequest request,
                               HttpServletResponse response,
                               Model model) {
        try {
            var user = userRepository.findByUsername(request.getUsername())
                    .filter(User::isActive)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITSTED));

            // Check quyền admin
            if (authenticationService.isAdmin(user)) {
                model.addAttribute("error", "Admin không được đăng nhập ở đây");
                return "login";
            }
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


}
