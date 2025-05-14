package com.example.english.demo.controller;

import com.example.english.demo.dto.response.CourseResponseDTO;
import com.example.english.demo.entity.CoursePayment;
import com.example.english.demo.entity.User;
import com.example.english.demo.service.CourseService;
import com.example.english.demo.service.UserService;
import com.example.english.demo.service.VNPayService;
import com.example.english.demo.service.CoursePaymentService;
import com.example.english.demo.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class VNPayController {

    private final VNPayService vnPayService;

    private final CoursePaymentService coursePaymentService;

    private final CourseService courseService;

    private final UserService userService;
    // @GetMapping("/payment/vnpay")
    // public ResponseEntity<String> testEndpoint(@RequestParam("courseId") Long courseId) {
    //     return ResponseEntity.ok("Endpoint works! CourseId: " + courseId);
    // }

   @GetMapping("/payment/vnpay")
   public void redirectToVnPay(@RequestParam("courseId") Long courseId,
                               HttpServletResponse response) throws Exception {

       // Note: Authentication check removed as per user request.
       // The user will not be available here if not logged in.
       // You may need to adjust how user information is handled for payment if required later.
       String userId = "anonymous"; // Or handle user identification differently if needed

       CourseResponseDTO course = courseService.getCourseById(courseId);
       if (course == null) {
           // Handle course not found, e.g., redirect to an error page or return an error response
           response.sendError(HttpServletResponse.SC_NOT_FOUND, "Course not found");
           return;
       }

       // Ép kiểu Double -> Float
       Float price = course.getPrice().floatValue();

       String orderInfo = "Thanh toán khóa học: " + course.getCourseName();
       String paymentUrl = vnPayService.createPaymentUrl(userId, course.getId(), price, orderInfo);

       response.sendRedirect(paymentUrl);
   }



    @PostMapping("/create-payment")
    public ResponseEntity<String> createPayment(@RequestParam String userId,
                                                @RequestParam Long courseId,
                                                @RequestParam Float amount) {
        String paymentUrl = vnPayService.createPaymentUrl(userId, courseId, amount, "Thanh toán khóa học");
        return ResponseEntity.ok(paymentUrl);
    }


    @GetMapping("/payment-return")
    public ResponseEntity<String> paymentReturn(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> params.put(key, values[0]));

        String receivedHash = params.remove("vnp_SecureHash");
        String calculatedHash = VNPayUtil.hashAllFields(params, vnPayService.getVnp_HashSecret());

        if (receivedHash != null && receivedHash.equals(calculatedHash)) {
            String orderId = params.get("vnp_TxnRef");
            Float amount = Float.parseFloat(params.get("vnp_Amount")) / 100;

            // Giả sử bạn đã có method lấy user và course theo orderId
            Optional<CoursePayment> existing = coursePaymentService.getPaymentByOrderId(orderId);
            if (existing.isEmpty()) {
                // Tùy hệ thống bạn có thể lấy user và course từ custom mapping nếu cần
                // coursePaymentService.savePayment(user, course, amount, orderId);
            }

            return ResponseEntity.ok("Giao dịch thành công");
        } else {
            return ResponseEntity.badRequest().body("Giao dịch không hợp lệ");
        }
    }


}
