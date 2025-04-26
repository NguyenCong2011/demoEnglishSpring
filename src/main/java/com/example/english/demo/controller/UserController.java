package com.example.english.demo.controller;

import com.example.english.demo.dto.request.ApiResponse;
import com.example.english.demo.dto.request.IntrospectRequest;
import com.example.english.demo.dto.request.UserCreateRequest;
import com.example.english.demo.dto.request.UserUpdateRequest;
import com.example.english.demo.dto.response.ToeicExamResponse;
import com.example.english.demo.dto.response.ToeicQuestionResponse;
import com.example.english.demo.dto.response.UserResponse;
import com.example.english.demo.entity.ToeicExam;
import com.example.english.demo.entity.ToeicQuestion;
import com.example.english.demo.entity.User;
import com.example.english.demo.exception.AppException;
import com.example.english.demo.exception.ErrorCode;
import com.example.english.demo.repository.ToeicExamRepository;
import com.example.english.demo.repository.ToeicQuestionRepository;
import com.example.english.demo.repository.UserRepository;
import com.example.english.demo.service.*;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;
    private final ToeicExamService toeicExamService;

    private final ToeicQuestionService toeicQuestionService;

    private  final ToeicExamRepository toeicExamRepository;

    private final ToeicQuestionRepository toeicQuestionRepository;


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


    @GetMapping("/toeic")
    public String getToeicExams(@RequestParam(defaultValue = "1") int pageNo, Model model) {
        Page<ToeicExamResponse> listToeicExams = toeicExamService.getToeicExams(pageNo);

        model.addAttribute("listToeicExams", listToeicExams);
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", listToeicExams.getTotalPages());

        return "user/toeic";
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

    @GetMapping("/show-toeic-question/{examId}")
    public String showAllQuestionsByExamId(
            @PathVariable Long examId,
            Model model) {

        // Lấy đề thi
        ToeicExam toeicExam = toeicExamRepository.findById(examId)
                .orElseThrow(() -> new AppException(ErrorCode.TOEIC_EXAM_NOT_EXITSTED));

        // ✅ Lấy tất cả các câu hỏi trong đề thi đó
        List<ToeicQuestionResponse> toeicQuestionResponses = toeicQuestionService.getToeicQuestionsByExamId(examId);

        model.addAttribute("toeicExam", toeicExam);
        model.addAttribute("toeicQuestions", toeicQuestionResponses);
        model.addAttribute("examId", examId);
        return "user/showExamQuestionByPart";
    }


    @PostMapping("/submit-toeic-exam")
    public String submitToeicExam(
            @RequestParam String examId,
            @RequestParam Map<String, String> answers,
            Model model) {

        Long parsedExamId = Long.parseLong(examId);
        List<ToeicQuestion> questions = toeicQuestionRepository.findByToeicExam_ExamId(parsedExamId);

        Map<Integer, Integer> partCorrect = new HashMap<>();
        Map<Integer, Integer> partTotal = new HashMap<>();

        int totalCorrect = 0;
        int totalQuestions = 0;

        for (ToeicQuestion question : questions) {
            Integer part = question.getPart();
            partTotal.put(part, partTotal.getOrDefault(part, 0) + 1);
            totalQuestions++;

            String key = "answers[" + question.getQuestionId() + "]";
            if (answers.containsKey(key)) {
                String selectedAnswer = answers.get(key);
                if (question.isCorrectAnswer(selectedAnswer)) {
                    partCorrect.put(part, partCorrect.getOrDefault(part, 0) + 1);
                    totalCorrect++;
                }
            }
        }

        List<Map<String, Object>> partResults = new ArrayList<>();
        for (Integer part : partTotal.keySet()) {
            Map<String, Object> result = new HashMap<>();
            result.put("part", part);
            result.put("correct", partCorrect.getOrDefault(part, 0));
            result.put("total", partTotal.get(part));
            partResults.add(result);
        }

        model.addAttribute("examId", examId);
        model.addAttribute("partResults", partResults);
        model.addAttribute("totalCorrect", totalCorrect);
        model.addAttribute("totalQuestions", totalQuestions);

        return "user/toeicExamResult";
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
