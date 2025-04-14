package com.example.english.demo.controller;

import com.example.english.demo.dto.request.ToeicExamCreateRequest;
import com.example.english.demo.dto.request.ToeicQuestionCreateRequest;
import com.example.english.demo.dto.response.ToeicQuestionResponse;
import com.example.english.demo.entity.ToeicExam;
import com.example.english.demo.exception.AppException;
import com.example.english.demo.exception.ErrorCode;
import com.example.english.demo.repository.ToeicExamRepository;
import com.example.english.demo.service.ToeicExamService;
import com.example.english.demo.service.ToeicQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.english.demo.dto.request.ApiResponse;
import com.example.english.demo.dto.response.ToeicExamResponse;
import com.example.english.demo.dto.response.UserResponse;
import com.example.english.demo.dto.request.UserCreateRequest;
import com.example.english.demo.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    public final ToeicQuestionService toeicQuestionService;

    public  final ToeicExamRepository toeicExamRepository;

    public  final ToeicExamService toeicExamService;

    public  final UserService userService;

    @GetMapping("/create-toeic-exam")
    public String showCreateToeicExamPage(Model model) {
        model.addAttribute("toeicExamCreateRequest", new ToeicExamCreateRequest());
        return "admin/createToeicExam";
    }

    @PostMapping("/create-toeic-exam")
    public String createToeicExam(@ModelAttribute @Valid ToeicExamCreateRequest toeicExamCreateRequest, Model model) {
        ApiResponse<ToeicExamResponse> apiResponse = new ApiResponse<>();
        try {
            ToeicExamResponse toeicExamResponse = toeicExamService.createToeicExam(toeicExamCreateRequest);
            apiResponse.setResult(toeicExamResponse);
            return "redirect:/admin/toeic";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create TOEIC exam");
            model.addAttribute("toeicExamCreateRequest", toeicExamCreateRequest);
            return "admin/createToeicExam";
        }
    }

    @GetMapping("/toeic")
    public String getToeicExams(@RequestParam(defaultValue = "1") int pageNo, Model model) {
        Page<ToeicExamResponse> listToeicExams = toeicExamService.getToeicExams(pageNo);

        model.addAttribute("listToeicExams", listToeicExams);
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages",listToeicExams.getTotalPages());

        return "admin/toeic";
    }

    @GetMapping("/create-toeic-question/{examId}")
    public String showCreateExamQuestionPage(@PathVariable Long examId, Model model) {
        ToeicExam toeicExam = toeicExamRepository.findById(examId)
                .orElseThrow(() -> new AppException(ErrorCode.TOEIC_EXAM_NOT_EXITSTED));

        model.addAttribute("toeicExam", toeicExam);
        model.addAttribute("examId", examId);
        model.addAttribute("toeicQuestionCreateRequests", new ArrayList<ToeicQuestionCreateRequest>());
        return "admin/createExamQuestion";
    }


    @PostMapping("/create-toeic-question/{examId}")
    public String createExamQuestion(@PathVariable Long examId,
                                     @ModelAttribute @Valid ToeicQuestionCreateRequest toeicQuestionCreateRequest,
                                     @RequestParam(value = "images", required = false) MultipartFile images,
                                     Model model) {
        try {
            // Gọi service để tạo câu hỏi TOEIC
            List<ToeicQuestionResponse> toeicQuestionResponses = toeicQuestionService.createToeicQuestions(
                    List.of(toeicQuestionCreateRequest), examId, images != null ? new MultipartFile[]{images} : new MultipartFile[0]
            );
            return "redirect:/admin/toeic";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create question");
            model.addAttribute("examId", examId);
            return "admin/createExamQuestion";
        }
    }

    @GetMapping("/show-toeic-question/{examId}")
    public String showQuestionsByExamIdAndPart(
            @PathVariable Long examId,
            @RequestParam(defaultValue = "1") Integer part,
            Model model) {

        List<ToeicQuestionResponse> toeicQuestionResponses = toeicQuestionService.getToeicQuestionsByPart(examId, part);

        model.addAttribute("toeicQuestions", toeicQuestionResponses);
        model.addAttribute("examId", examId);
        model.addAttribute("part", part);
        return "admin/showExamQuestionByPart";
    }


    @PostMapping("/import-toeic-questions/{examId}")
    public String importToeicQuestions(@PathVariable Long examId,
                                       @RequestParam("file") MultipartFile file,
                                       Model model) {
        try {
            toeicQuestionService.importToeicQuestionsFromExcel(file, examId);
            return "redirect:/admin/toeic";
        } catch (AppException e) {
            model.addAttribute("error", "Import thất bại: " + e.getMessage());
            model.addAttribute("examId", examId);
            return "admin/showExamQuestionByPart"; // hoặc trang hiển thị danh sách câu hỏi
        }
    }


}
