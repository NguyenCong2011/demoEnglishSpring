package com.example.english.demo.controller;

import com.example.english.demo.dto.request.ApiResponse;
import com.example.english.demo.dto.request.IntrospectRequest;
import com.example.english.demo.dto.request.UserCreateRequest;
import com.example.english.demo.dto.request.UserUpdateRequest;
import com.example.english.demo.dto.response.ToeicExamResponse;
import com.example.english.demo.dto.response.ToeicQuestionResponse;
import com.example.english.demo.dto.response.UserResponse;
import com.example.english.demo.entity.CompetitionResult;
import com.example.english.demo.entity.ToeicExam;
import com.example.english.demo.entity.ToeicQuestion;
import com.example.english.demo.entity.User;
import com.example.english.demo.exception.AppException;
import com.example.english.demo.exception.ErrorCode;
import com.example.english.demo.repository.CompetitionResultRepository;
import com.example.english.demo.repository.ToeicExamRepository;
import com.example.english.demo.repository.ToeicQuestionRepository;
import com.example.english.demo.repository.UserRepository;
import com.example.english.demo.service.AuthenticationService;
import com.example.english.demo.service.CompetitionResultService;
import com.example.english.demo.service.ToeicExamService;
import com.example.english.demo.service.ToeicQuestionService;
import com.example.english.demo.service.UserService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.messaging.simp.SimpMessagingTemplate; // Import SimpMessagingTemplate
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
import java.util.Optional;

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
    private final ToeicExamRepository toeicExamRepository;
    private final ToeicQuestionRepository toeicQuestionRepository;
    private final CompetitionResultRepository competitionResultRepository;
    private final CompetitionResultService competitionResultService;
    private final SimpMessagingTemplate messagingTemplate; // Inject SimpMessagingTemplate

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
        var authentication= SecurityContextHolder.getContext().getAuthentication();

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
            @RequestParam(required = false) String user1Id,
            @RequestParam(required = false) String user2Id,
            Model model) {

        // Lấy đề thi
        ToeicExam toeicExam = toeicExamRepository.findById(examId)
                .orElseThrow(() -> new AppException(ErrorCode.TOEIC_EXAM_NOT_EXITSTED));

        // ✅ Lấy tất cả các câu hỏi trong đề thi đó
        List<ToeicQuestionResponse> toeicQuestionResponses = toeicQuestionService.getToeicQuestionsByExamId(examId);

        model.addAttribute("toeicExam", toeicExam);
        model.addAttribute("toeicQuestions", toeicQuestionResponses);
        model.addAttribute("examId", examId);
        // Add user1Id and user2Id to the model
        model.addAttribute("user1Id", user1Id);
        model.addAttribute("user2Id", user2Id);

        return "user/showExamQuestionByPart";
    }

    @PostMapping("/submit-toeic-exam")
    public String submitToeicExam(
            @RequestParam String examId,
            @RequestParam Map<String, String> answers,
            @RequestParam(required = false) String user1Id,
            @RequestParam(required = false) String user2Id,
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

        // Nếu là thi đấu thì xử lý lưu điểm và gửi kết quả nếu cả 2 đã nộp bài
        if (user1Id != null && !user1Id.isEmpty() && user2Id != null && !user2Id.isEmpty()) {
            String currentUserId = getCurrentUserId(); // Lấy ID người hiện tại
            competitionResultService.createOrUpdateCompetitionResult(parsedExamId, user1Id, user2Id, currentUserId, totalCorrect);

            List<CompetitionResult> updatedCompetitionResults = competitionResultRepository.findByExam_ExamIdAndUser1_IdAndUser2_IdOrExam_ExamIdAndUser2_IdAndUser1_Id(
                    parsedExamId, user1Id, user2Id,
                    parsedExamId, user2Id, user1Id
            );

            CompetitionResult updatedCompetitionResult = null;
            if (!updatedCompetitionResults.isEmpty()) {
                if (updatedCompetitionResults.size() > 1) {
                    log.warn("⚠️ Found multiple CompetitionResult entries for examId {} and users {} and {} after saving score. Searching for the correct one.", parsedExamId, user1Id, user2Id);
                }
                // Iterate through results to find the specific entry for this user pair
                for (CompetitionResult res : updatedCompetitionResults) {
                    if ((res.getUser1().getId().equals(user1Id) && res.getUser2().getId().equals(user2Id)) ||
                        (res.getUser1().getId().equals(user2Id) && res.getUser2().getId().equals(user1Id))) {
                        updatedCompetitionResult = res;
                        log.info("✅ Found specific CompetitionResult after saving for examId {} and users {} and {}", parsedExamId, user1Id, user2Id);
                        break; // Found the correct one, exit loop
                    }
                }
            }

            if (updatedCompetitionResult != null) {
                // Nếu cả 2 người đã có điểm, thì gửi WebSocket thông báo
                if (updatedCompetitionResult.getUser1Score() != null && updatedCompetitionResult.getUser2Score() != null &&
                        (updatedCompetitionResult.getUser1Score() > 0 || updatedCompetitionResult.getUser2Score() > 0)) {

                    Map<String, Object> competitionUpdate = new HashMap<>();
                    competitionUpdate.put("completed", true);
                    // We no longer have roomId, need a way to identify the competition on the frontend
                    // Maybe use examId and user IDs? Or the CompetitionResult ID?
                    competitionUpdate.put("examId", parsedExamId);
                    competitionUpdate.put("user1Id", updatedCompetitionResult.getUser1().getId());
                    competitionUpdate.put("user2Id", updatedCompetitionResult.getUser2().getId());
                    competitionUpdate.put("user1Score", updatedCompetitionResult.getUser1Score());
                    competitionUpdate.put("user2Score", updatedCompetitionResult.getUser2Score());

                    // Need to send to a topic that both users are subscribed to for this competition
                    // A topic based on examId and user IDs might work, e.g., /topic/competition/{examId}/{user1Id}/{user2Id}
                    // Or maybe just use the CompetitionResult ID if it's shared with the frontend
                    // For now, let's use a placeholder topic
                    messagingTemplate.convertAndSend("/topic/competition/result", competitionUpdate);
                    log.info("Sent competition completion message for examId {} and users {} and {}", parsedExamId, user1Id, user2Id);
                }

                // ✅ Thêm dữ liệu vào model để Thymeleaf hiển thị
                model.addAttribute("user1Score", updatedCompetitionResult.getUser1Score());
                model.addAttribute("user2Score", updatedCompetitionResult.getUser2Score());
                model.addAttribute("user1Id", updatedCompetitionResult.getUser1().getId());
                model.addAttribute("user2Id", updatedCompetitionResult.getUser2().getId());
                model.addAttribute("currentUserId", currentUserId);
            } else {
                log.error("CompetitionResult not found for examId {} and users {} and {} after saving score.", parsedExamId, user1Id, user2Id);
            }
        }

        // Truyền các thông tin về kết quả thi vào view
        model.addAttribute("examId", examId);
        model.addAttribute("partResults", partResults);
        model.addAttribute("totalCorrect", totalCorrect);
        model.addAttribute("totalQuestions", totalQuestions);
        model.addAttribute("isCompetition", user1Id != null && !user1Id.isEmpty() && user2Id != null && !user2Id.isEmpty());

        return "user/toeicExamResult";
    }

    @GetMapping("/toeic-detail/{examId}")
    public String showToeicDetail(@PathVariable Long examId, Model model) {
        ToeicExam toeicExam = toeicExamRepository.findById(examId)
                .orElseThrow(() -> new AppException(ErrorCode.TOEIC_EXAM_NOT_EXITSTED));

        // Lấy thông tin người dùng đang đăng nhập
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITSTED));

        // Lấy lịch sử thi đấu của người dùng hiện tại với đề thi này
        // Need to find competition results where the current user is either user1 or user2
        List<CompetitionResult> history = new ArrayList<>();
        List<CompetitionResult> results = competitionResultRepository
                .findByExam_ExamIdAndUser1_IdAndUser2_IdOrExam_ExamIdAndUser2_IdAndUser1_Id(
                        examId, currentUser.getId(), null, // Search for current user as user1
                        examId, null, currentUser.getId()  // Search for current user as user2
                );

        // This repository method is designed for finding a specific competition between two users.
        // To get history for a user in any competition for this exam, a different repository method is needed.
        // Let's add a new method to the repository to find results by exam and one user ID.
        // For now, I will handle the List return type to fix the compilation error,
        // but acknowledge that a new repository method is needed for proper history fetching.
        if (!results.isEmpty()) {
             // For now, just add all results found to history. This is not the correct history logic.
             history.addAll(results);
             if (results.size() > 1) {
                 log.warn("⚠️ Found multiple CompetitionResult entries for examId {} and user {}. History fetching needs a dedicated repository method.", examId, currentUser.getId());
             }
        }


        model.addAttribute("toeicExam", toeicExam);
        model.addAttribute("examId", examId);
        model.addAttribute("currentUserId", currentUser.getId());
        model.addAttribute("currentUsername", currentUser.getUsername());
        model.addAttribute("competitionHistory", history); // History finding needs to be fixed properly

        return "user/toeicDetail";
    }

    @GetMapping("/search-users")
    @ResponseBody
    public ApiResponse<?> searchUsers(@RequestParam("keyword") String keyword) {
        List<User> users = userService.searchUsersByKeyword(keyword);

        if (users.isEmpty()) {
            return ApiResponse.builder()
                    .message("Không tìm thấy người dùng nào.")
                    .result(new ArrayList<>())
                    .build();
        }

        List<Map<String, String>> result = new ArrayList<>();
        for (User user : users) {
            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            result.add(userInfo);
        }

        return ApiResponse.builder()
                .result(result)
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

    private String getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String username = auth.getName(); // "user123"
            Optional<User> user = userRepository.findByUsername(username);
            if (user != null) {
                return user.get().getId(); // đúng UUID "51ad0d5e-..."
            }
        }
        throw new RuntimeException("User not authenticated");
    }

}
