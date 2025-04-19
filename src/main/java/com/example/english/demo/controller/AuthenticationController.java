package com.example.english.demo.controller;

import com.example.english.demo.dto.request.*;
import com.example.english.demo.dto.response.AuthenticationResponse;
import com.example.english.demo.dto.response.IntrospectResponse;
import com.example.english.demo.dto.response.UserResponse;
import com.example.english.demo.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.text.ParseException;
//
//@RestController
//@RequestMapping("/auth")
//@RequiredArgsConstructor//dùng để áp bean
//public class AuthenticationController {
//    private final AuthenticationService authenticationService;
//
//    @PostMapping("/login")
//    private ApiResponse<AuthenticationResponse> authenticated(@RequestBody AuthenticationRequest request){
//        boolean result = authenticationService.authenticate(request);
//
//        return ApiResponse.<AuthenticationResponse>builder()
//                .result(AuthenticationResponse.builder()
//                        .authenticated(result)
//                        .build())
//                .build();
//    }
//}
//@RestController
//@RequestMapping("/auth")
//@RequiredArgsConstructor
//public class AuthenticationController {
//
//    private final AuthenticationService authenticationService;
//
//    @PostMapping("/login")
//    public ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
//        boolean result = authenticationService.authenticate(request);
//
//        AuthenticationResponse authResponse = AuthenticationResponse.builder()
//                .authenticated(result)
//                .build();
//
//        return ApiResponse.<AuthenticationResponse>builder()
//                .result(authResponse)
//                .build();
//    }
//}

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;


    @GetMapping("/logout")
    public ModelAndView logout(HttpServletRequest request, HttpServletResponse response, ModelAndView modelAndView) throws ParseException, JOSEException {
        // Lấy token từ cookie
        String token = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // Nếu có token thì gọi service để blacklist
        if (token != null && !token.isEmpty()) {
            authenticationService.logout(new LogoutRequest(token));
        }

        // Xóa cookie
        Cookie cookie = new Cookie("jwt", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        // Chuyển hướng về trang chủ
        modelAndView.setViewName("redirect:/");
        return modelAndView;
    }


    @PostMapping("/verify-token")
    public ApiResponse<IntrospectResponse> verifyToken(@RequestBody IntrospectRequest request) throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);

        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }


    @PostMapping("/refesh-token")
    public ApiResponse<AuthenticationResponse> refeshToken(@RequestBody RefeshRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.refeshToken(request);

        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }
}

