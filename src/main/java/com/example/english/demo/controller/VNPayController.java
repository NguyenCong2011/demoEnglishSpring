package com.example.english.demo.controller;

import com.example.english.demo.dto.request.CoursePaymentRequest;
import com.example.english.demo.dto.response.CourseResponseDTO;
import com.example.english.demo.entity.Course;
import com.example.english.demo.entity.CoursePayment;
import com.example.english.demo.enums.PaymentStatus;
import com.example.english.demo.repository.CoursePaymentRepository;
import com.example.english.demo.repository.CourseRepository;
import com.example.english.demo.service.CoursePaymentService;
import com.example.english.demo.service.CourseService;
import com.example.english.demo.service.VNPayService;
import com.example.english.demo.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import com.example.english.demo.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.english.demo.entity.User;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger; // Import Logger

@Controller
@RequiredArgsConstructor
public class VNPayController {

    private static final Logger logger = Logger.getLogger(VNPayController.class.getName()); // Add Logger


    private final VNPayService vnPayService;

    private final CourseService courseService;

    private final CourseRepository courseRepository;

    private final CoursePaymentRepository coursePaymentRepository;
    private final UserRepository userRepository; // Inject UserRepository

    private final CoursePaymentService coursePaymentService;

   @GetMapping("/payment/vnpay")
   public void redirectToVnPay(@RequestParam("courseId") Long courseId,
                               HttpServletResponse response) throws Exception {

       logger.info("Redirecting to VNPay for courseId: " + courseId); // Log entry

       // Get authenticated user's username
       Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
       String username = null;
       if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
           UserDetails userDetails = (UserDetails) authentication.getPrincipal();
           username = userDetails.getUsername();
           logger.info("Authenticated username: " + username); // Log username
       } else {
           logger.warning("User not authenticated for payment redirect."); // Log if not authenticated
       }

       String userId = username != null ? username : "anonymous"; // Use username or anonymous
       logger.info("Using userId for VNPay: " + userId); // Log userId


       CourseResponseDTO course = courseService.getCourseById(courseId);
       if (course == null) {
           response.sendError(HttpServletResponse.SC_NOT_FOUND, "Course not found");
           return;
       }

       Float price = course.getPrice().floatValue();

       String orderInfo = "courseId:" + course.getId() + " - Thanh toán khóa học: " + course.getCourseName(); // Include courseId in orderInfo
       logger.info("Generated Order Info for VNPay: " + orderInfo); // Log generated order info
       String paymentUrl = vnPayService.createPaymentUrl(userId, course.getId(), price, orderInfo);

       response.sendRedirect(paymentUrl);
   }


    @GetMapping("/payment-callback")
    public String paymentReturn(HttpServletRequest request, Model model) {
        logger.info("Entering paymentReturn callback."); // Log entry

        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> params.put(key, values[0]));

        logger.info("Received parameters: " + params); // Log received parameters

        String receivedHash = params.remove("vnp_SecureHash");
        String calculatedHash = VNPayUtil.hashAllFields(params, vnPayService.getVnp_HashSecret());

        logger.info("Received Hash: " + receivedHash); // Log hashes
        logger.info("Calculated Hash: " + calculatedHash);

        if (receivedHash != null && receivedHash.equals(calculatedHash)) {
            logger.info("Hash validation successful."); // Log hash success
            String responseCode = params.get("vnp_ResponseCode");
            logger.info("VNPay Response Code: " + responseCode); // Log response code

            if ("00".equals(responseCode)) {
                model.addAttribute("status", "success");
                logger.info("Payment successful (Response Code 00)."); // Log success

                String orderInfo = params.get("vnp_OrderInfo"); // ex: "courseId:2 - Thanh toán khóa học: ..."
                logger.info("Order Info: " + orderInfo); // Log order info

                if (orderInfo != null && orderInfo.startsWith("courseId:")) {
                    try {
                        String orderInfoWithoutPrefix = orderInfo.substring("courseId:".length());
                        String[] parts = orderInfoWithoutPrefix.split(" - ", 2); // Split by " - "

                        if (parts.length > 0) {
                            Long courseId = Long.parseLong(parts[0]); // Get the courseId part
                            logger.info("Parsed Course ID: " + courseId); // Log parsed course ID

                            Course course = courseRepository.findById(courseId).orElse(null);

                            if (course != null) {
                                logger.info("Course found: " + course.getCourseName()); // Log course found

                                // Get authenticated user's username
                                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                                String username = null;
                                if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                                    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                                    username = userDetails.getUsername();
                                    logger.info("Authenticated username in callback: " + username); // Log username in callback
                                } else {
                                    logger.warning("User not authenticated in payment callback."); // Log if not authenticated
                                }

                                User currentUser = null;
                                if (username != null) {
                                    // Fetch the User entity from the database
                                    Optional<User> userOptional = userRepository.findByUsername(username);
                                    if (userOptional.isPresent()) {
                                        currentUser = userOptional.get();
                                        logger.info("User entity found for username: " + username); // Log user entity found
                                    } else {
                                        logger.warning("User entity not found for username: " + username); // Log if user entity not found
                                    }
                                } else {
                                    logger.warning("Username is null, cannot fetch user entity."); // Log if username is null
                                }


                                // Tạo request DTO
                                CoursePaymentRequest requestDto = new CoursePaymentRequest();
                                requestDto.setOrder(params.get("vnp_TxnRef"));
                                requestDto.setAmount(Float.parseFloat(params.get("vnp_Amount")) / 100);
                                requestDto.setSuccess(true);
                                requestDto.setCreatedAt(LocalDateTime.now());
                                requestDto.setCourse(course);
                                requestDto.setStatus(PaymentStatus.SUCCESS);
                                requestDto.setUser(currentUser); // Set the fetched user entity

                                logger.info("CoursePaymentRequest created: " + requestDto); // Log request DTO

                                coursePaymentService.createCoursePayment(requestDto);
                                logger.info("createCoursePayment service called."); // Log service call

                            } else {
                                logger.warning("Course not found for ID: " + courseId); // Log if course not found
                            }
                        } else {
                            logger.warning("Order Info does not contain a valid courseId part: " + orderInfo); // Log if courseId part is missing
                        }
                    } catch (NumberFormatException e) {
                        logger.severe("Error parsing courseId from OrderInfo: " + orderInfo + " - " + e.getMessage()); // Log parsing error
                    }
                } else {
                    logger.warning("Order Info does not start with 'courseId:' or is null: " + orderInfo); // Log invalid order info
                }
            } else {
                model.addAttribute("status", "fail");
                logger.warning("Payment failed or pending. Response Code: " + responseCode); // Log payment failure
            }
        } else {
            model.addAttribute("status", "invalid");
            logger.severe("Hash validation failed. Received: " + receivedHash + ", Calculated: " + calculatedHash); // Log hash failure
        }

        logger.info("Exiting paymentReturn callback, returning view: paymentResult"); // Log exit
        return "paymentResult";
    }

}
