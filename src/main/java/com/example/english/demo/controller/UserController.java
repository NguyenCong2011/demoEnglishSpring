package com.example.english.demo.controller;

import org.springframework.stereotype.Controller;

import com.example.english.demo.dto.request.ApiResponse;
import com.example.english.demo.dto.request.UserCreateRequest;
import com.example.english.demo.dto.request.UserUpdateRequest;
import com.example.english.demo.dto.response.UserResponse;
import com.example.english.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    @PostMapping("/create")
    public String createUser(@ModelAttribute @Valid UserCreateRequest request, Model model) {
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        try {
            UserResponse userResponse = userService.createUser(request);
            apiResponse.setResult(userResponse);
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create user");
            model.addAttribute("userCreateRequest", request);
            return "createUser";
        }
    }

    @GetMapping("/create")
    public String createUserPage(Model model) {
        return "createUser";
    }

    @GetMapping("/getAllUser")
    ApiResponse<List<UserResponse>> getUser(){
        var authentication= SecurityContextHolder.getContext().getAuthentication();//lấy thông tin user đnag đăng nhập giống như bên odoo vậy

        log.info("Username: {}",authentication.getName());
        authentication.getAuthorities().forEach(grantedAuthority -> log.info(grantedAuthority.getAuthority()));
        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getUser()).build();
    }

    @GetMapping("/{userId}")
    @ResponseBody
    ApiResponse<UserResponse> getUserById(@PathVariable("userId") String userId){
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUserById(userId))
                .build();
    }


    @PutMapping("/{userId}")
    @ResponseBody
    ApiResponse<UserResponse> updateUser(@PathVariable String userId,@RequestBody UserUpdateRequest request){
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUser(userId,request))
                .build();
    }

    @DeleteMapping("/{userId}")
    @ResponseBody
    String deleteUser(@PathVariable String userId){
        userService.deleteUser(userId);
        return "User has beeen deleted";
    }

    @GetMapping("/myInfo")
    @ResponseBody
    ApiResponse<UserResponse> getMyInfo(){
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
    }

}
