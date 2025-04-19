package com.example.english.demo.controller;

import com.example.english.demo.dto.request.IntrospectRequest;
import com.example.english.demo.entity.User;
import com.example.english.demo.repository.UserRepository;
import com.example.english.demo.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Controller;

import com.example.english.demo.dto.request.ApiResponse;
import com.example.english.demo.dto.request.UserCreateRequest;
import com.example.english.demo.dto.request.UserUpdateRequest;
import com.example.english.demo.dto.response.UserResponse;
import com.example.english.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.text.ParseException;
import java.util.List;
@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;

//    @PostMapping("/create")
//    public String createUser(@ModelAttribute @Valid UserCreateRequest request, Model model) {
//        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
//        try {
//            UserResponse userResponse = userService.createUser(request);
//            apiResponse.setResult(userResponse);
//            return "redirect:/login";
//        } catch (Exception e) {
//            model.addAttribute("error", "Failed to create user");
//            model.addAttribute("userCreateRequest", request);
//            return "createUser";
//        }
//    }

//    @PostMapping("/create")
//    public String createUser(@ModelAttribute @Valid UserCreateRequest request, Model model) {
//        try {
//            userService.sendConfirmationEmail(request.getEmail());
//            model.addAttribute("message", "Đã gửi email xác nhận. Vui lòng kiểm tra hộp thư.");
//            return "createUser"; // Hiển thị lại form + thông báo
//        } catch (Exception e) {
//            model.addAttribute("error", "Không thể gửi email xác nhận.");
//            return "createUser";
//        }
//    }

    @GetMapping("/create")
    public String createUserPage(Model model) {
        model.addAttribute("userCreateRequest",new UserCreateRequest());
        return "createUser";
    }

    @PostMapping("/create")
    public ModelAndView createUser(UserCreateRequest request, ModelAndView modelAndView) {
        User existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser != null) {
            modelAndView.addObject("message", "This email already exists!");
            modelAndView.setViewName("error");
        } else {
            userService.createUser(request);
            modelAndView.addObject("emailId",request.getEmail());
            modelAndView.setViewName("successfulRegisstration");
        }
        return modelAndView;
    }

    @GetMapping("/confirm-account")
    public ModelAndView confirmEmail(@RequestParam("token") String confirmToken,
                                     ModelAndView modelAndView) throws ParseException, JOSEException {

        var request = IntrospectRequest.builder().token(confirmToken).build();
        var response = authenticationService.introspect(request);

        if (response.isValid()) {
            SignedJWT signedJWT = SignedJWT.parse(confirmToken);
            String email = signedJWT.getJWTClaimsSet().getSubject();

            User user = userRepository.findByEmail(email);
            user.setActive(true);
            userRepository.save(user);

            modelAndView.addObject("message", "Tài khoản đã được xác nhận thành công!");
            modelAndView.setViewName("accountVerified");
        } else {
            modelAndView.addObject("message", "Token không hợp lệ hoặc đã hết hạn.");
            modelAndView.setViewName("error");
        }

        return modelAndView;
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
