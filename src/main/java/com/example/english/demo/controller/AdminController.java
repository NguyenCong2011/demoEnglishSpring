package com.example.english.demo.controller;

import com.example.english.demo.dto.request.*;
import com.example.english.demo.dto.response.CourseResponseDTO;
import com.example.english.demo.dto.response.ToeicExamResponse;
import com.example.english.demo.dto.response.ToeicQuestionResponse;
import com.example.english.demo.entity.DomainInfo;
import com.example.english.demo.entity.PropertyInfo;
import com.example.english.demo.entity.ToeicExam;
import com.example.english.demo.entity.User;
import com.example.english.demo.exception.AppException;
import com.example.english.demo.exception.ErrorCode;
import com.example.english.demo.repository.ToeicExamRepository;
import com.example.english.demo.repository.UserRepository;
import com.example.english.demo.service.*;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest; // Added import
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.hibernate.cfg.C3p0Settings;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ToeicQuestionService toeicQuestionService;

    private  final ToeicExamRepository toeicExamRepository;

    private  final ToeicExamService toeicExamService;

    private final FileUploadService fileUploadService;

    private final AuthenticationService authenticationService;

    private final UserRepository userRepository;

    private final CourseService courseService;

    private final CloudinaryService cloudinaryService;

    private final ObjectMapper objectMapper;

    @GetMapping("/login")
    public String showAdminLoginPage() {
        return "admin/login";
    }

    @PostMapping("/login")
    public String processAdminLogin(@ModelAttribute @Valid LoginRequest request,
                                    HttpServletRequest httpRequest, // Added HttpServletRequest
                                    HttpServletResponse response,
                                    Model model) {
        try {
            var user = userRepository.findByUsername(request.getUsername())
                    .filter(User::isActive)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITSTED));

            // Check quyền admin
            if (!authenticationService.isAdmin(user)) {
                model.addAttribute("error", "Bạn không có quyền truy cập trang admin!");
                return "admin/login";
            }

            // Nếu là admin thì tiếp tục xác thực nhé
            var result = authenticationService.authenticate(request);

            if (result.isAuthenticated()) {
                // Remove any existing 'jwt' cookie
                Cookie[] cookies = httpRequest.getCookies(); // Changed request to httpRequest
                if (cookies != null) {
                    for (Cookie oldCookie : cookies) {
                        if ("jwt".equals(oldCookie.getName())) {
                            oldCookie.setValue("");
                            oldCookie.setPath("/");
                            oldCookie.setMaxAge(0);
                            response.addCookie(oldCookie);
                            break;
                        }
                    }
                }

                // Set the new 'jwt' cookie for the admin
                Cookie cookie = new Cookie("jwt", result.getToken());
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                cookie.setMaxAge(7 * 24 * 60 * 60);
                response.addCookie(cookie);

                return "redirect:/";
            } else {
                model.addAttribute("error", "Authentication failed");
                return "admin/login";
            }

        } catch (Exception e) {
            model.addAttribute("error", "Sai tài khoản hoặc mật khẩu hoặc không phải admin.");
            return "admin/login";
        }
    }

    @GetMapping("/create-toeic-exam")
    public String showCreateToeicExamPage(Model model) {
        model.addAttribute("toeicExamCreateRequest", new ToeicExamCreateRequest());
        return "admin/createToeicExam";
    }


    @PostMapping("/create-toeic-exam")
    public String createToeicExam(@ModelAttribute @Valid ToeicExamCreateRequest toeicExamCreateRequest,
                                  Model model) {
        try {
            MultipartFile audioFile = toeicExamCreateRequest.getAudioFile();
            boolean isCloudinary = Boolean.TRUE.equals(toeicExamCreateRequest.getIsCloudinary());

            String audioFileName;
            if (isCloudinary) {
                // Gọi Cloudinary upload
                audioFileName = cloudinaryService.uploadAudio(audioFile); // bạn cần viết hàm này
            } else {
                // Gọi upload local
                audioFileName = fileUploadService.uploadAudioFile(audioFile);
            }

            toeicExamCreateRequest.setAudio(audioFileName);
            ToeicExamResponse toeicExamResponse = toeicExamService.createToeicExam(toeicExamCreateRequest);

            return "redirect:/admin/toeic";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create TOEIC exam: " + e.getMessage());
            model.addAttribute("toeicExamCreateRequest", toeicExamCreateRequest);
            return "admin/createToeicExam"; // Đây là view form (tên file .html)
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

        // ✅ Lấy thông tin đề thi
        ToeicExam toeicExam = toeicExamRepository.findById(examId)
                .orElseThrow(() -> new AppException(ErrorCode.TOEIC_EXAM_NOT_EXITSTED));

        List<ToeicQuestionResponse> toeicQuestionResponses = toeicQuestionService.getToeicQuestionsByPart(examId, part);

        // ✅ Truyền thêm vào model
        model.addAttribute("toeicExam", toeicExam);
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


    @PostMapping("/update-question-image/{questionId}")
    public String updateQuestionImage(@PathVariable Long questionId,
                                      @RequestParam("imageFile") MultipartFile imageFile,
                                      @RequestParam("examId") Long examId,
                                      @RequestParam("part") Integer part,
                                      @RequestParam(value = "isCloudinary", required = false) Boolean isCloudinary,
                                      RedirectAttributes redirectAttributes) {
        try {
            ToeicQuestionUpdateRequest request = new ToeicQuestionUpdateRequest();
            request.setIsCloudinary(isCloudinary != null && isCloudinary); // gán true/false an toàn

            toeicQuestionService.updateToeicQuestion(questionId, request, imageFile);
        } catch (AppException e) {
            redirectAttributes.addFlashAttribute("error", e.getErrorCode().getMessage());
        }
        return "redirect:/admin/show-toeic-question/" + examId + "?part=" + part;
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/createCourse")
    public String showCreateCourseForm(Model model) {
        model.addAttribute("courseRequestDTO", new CourseRequestDTO());
        return "admin/createCourse";
    }

    @PostMapping("/createCourse")
    public String createCourse(@ModelAttribute @Valid CourseRequestDTO courseRequestDTO,
                               Model model) {
        try {
            CourseResponseDTO response = courseService.createCourse(courseRequestDTO);
            return "redirect:/";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create course");
            return "admin/createCourse";
        }
    }

    public List<DomainInfo> extractClass(Class<?> clazz) {
        // Set to keep track of visited classes to avoid infinite loops (e.g., circular references)
        Set<Class<?>> visited = new HashSet<>();
        
        // List to store the final result of extracted domain information
        List<DomainInfo> domainInfos = new ArrayList<>();
        
        // Stack for iterative traversal of class hierarchy (DFS style)
        Deque<Class<?>> stack = new ArrayDeque<>();
        
        // Start with the input class
        stack.push(clazz);

        // Continue processing until the stack is empty
        while (!stack.isEmpty()) {
            // Pop the current class from the stack
            Class<?> current = stack.pop();
            
            // Skip if class is null, a simple type (primitive, String, etc.), or already visited
            if (current == null || isSimpleType(current) || visited.contains(current)) {
                continue;
            }

            // Mark current class as visited
            visited.add(current);
            
            // List to store properties of the current class
            List<PropertyInfo> currentClassProperties = new ArrayList<>();
            
            // Convert class to Jackson JavaType
            JavaType javaType = objectMapper.constructType(current);
            
            // Introspect class to get property metadata using Jackson
            var beanDesc = objectMapper.getSerializationConfig()
                    .introspect(javaType);

            // Iterate through all detected properties
            for (BeanPropertyDefinition prop : beanDesc.findProperties()) {
                
                // Skip if property has no getter/accessor
                if (prop.getAccessor() == null) {
                    continue;
                }

                // Get property name
                String propertyName = prop.getName();
                
                // Get property type
                JavaType type = prop.getPrimaryType();
                if (type == null) {
                    continue;
                }

                // Determine data type as string (handle collections like List<T>)
                String dataType;
                if (type.isContainerType() && type.getContentType() != null) {
                    dataType = type.getRawClass().getSimpleName()
                            + "<" + type.getContentType().getRawClass().getSimpleName() + ">";
                } else {
                    dataType = type.getRawClass().getSimpleName();
                }

                // Add property info (name + type) to current class property list
                currentClassProperties.add(new PropertyInfo(propertyName, dataType));
                
                // Determine child class (for nested objects or collection element type)
                Class<?> childClass = (type.isContainerType() && type.getContentType() != null)
                        ? type.getContentType().getRawClass()
                        : type.getRawClass();

                // If child class is not simple and not visited, push it to stack for further processing
                if (!isSimpleType(childClass) && !visited.contains(childClass)) {
                    stack.push(childClass);
                }
            }

            // Add current class info (class name + properties) to result list
            domainInfos.add(new DomainInfo(
                    current.getSimpleName(),
                    currentClassProperties
            ));

            // Also process superclass if it exists and hasn't been visited
            Class<?> superClazz = current.getSuperclass();
            if (superClazz != null
                    && superClazz != Object.class
                    && !visited.contains(superClazz)) {
                stack.push(superClazz);
            }
        }
        
        // Return the collected domain information
        return domainInfos;
    }

    private boolean isSimpleType(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            return true;
        }
        return true;
    }
}


SELECT C4.* T.APIURI
FROM CTRANS04 C4
LEFT JOIN(
    SELECT C4.DomainId ,LISTAGG(C2.ApiUri,',') ASS APIURI
    FROM CTRANS03 C3
    LEFT JOIN CTRANS02 C2 ON C2.ApiId=C3.ApiId
    LEFT JOIN CTRANS04 C4 ON C4.DomainId=C3.DomainId
    WHERE C4.SystemId=#{_SystemId,jdbcType=VARCAHR}
    GROUPBY C4.DomainID
)T ON C4.DomainId=T.DomainId
WHERE C4.SystemId=#{_SystemId,jdbcType=VARCAHR}