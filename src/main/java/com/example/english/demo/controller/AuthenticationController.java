package com.example.english.demo.controller;

import com.example.english.demo.dto.request.*;
import com.example.english.demo.dto.response.AuthenticationResponse;
import com.example.english.demo.dto.response.IntrospectResponse;
import com.example.english.demo.dto.response.UserResponse;
import com.example.english.demo.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/login")
    public String authenticate(@ModelAttribute @Valid AuthenticationRequest request, Model model) {
        ApiResponse<AuthenticationResponse> apiResponse = new ApiResponse<>();

        try {
            var result = authenticationService.authenticate(request);

            if (result.isAuthenticated()) {
                apiResponse.setResult(result);
                return "redirect:/";
            } else {
                model.addAttribute("error", "Invalid username or password!");
                return "login";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Authentication failed");
            return "login";
        }
    }


    @PostMapping("/verify-token")
    public ApiResponse<IntrospectResponse> verifyToken(@RequestBody IntrospectRequest request) throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);

        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        authenticationService.logout(request);

        return ApiResponse.<Void>builder()
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

