package com.example.english.demo.controller;

import com.example.english.demo.dto.response.CourseResponseDTO;
import com.example.english.demo.entity.CoursePayment;
import com.example.english.demo.service.CourseService;
import com.example.english.demo.service.UserService;
import com.example.english.demo.service.VNPayService;
import com.example.english.demo.service.CoursePaymentService;
import com.example.english.demo.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class VNPayController {

    private final VNPayService vnPayService;

    private final CoursePaymentService coursePaymentService;

    private final CourseService courseService;

   @GetMapping("/payment/vnpay")
   public void redirectToVnPay(@RequestParam("courseId") Long courseId,
                               HttpServletResponse response) throws Exception {

       String userId = "anonymous";

       CourseResponseDTO course = courseService.getCourseById(courseId);
       if (course == null) {
           response.sendError(HttpServletResponse.SC_NOT_FOUND, "Course not found");
           return;
       }

       Float price = course.getPrice().floatValue();

       String orderInfo = "Thanh toán khóa học: " + course.getCourseName();
       String paymentUrl = vnPayService.createPaymentUrl(userId, course.getId(), price, orderInfo);

       response.sendRedirect(paymentUrl);
   }


    @GetMapping("/payment-callback")
    public ResponseEntity<String> paymentReturn(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> params.put(key, values[0]));

        String receivedHash = params.remove("vnp_SecureHash");
        String calculatedHash = VNPayUtil.hashAllFields(params, vnPayService.getVnp_HashSecret());

        if (receivedHash != null && receivedHash.equals(calculatedHash)) {
            return ResponseEntity.ok("Giao dịch thành công");
        } else {
            return ResponseEntity.badRequest().body("Giao dịch không hợp lệ");
        }
    }


}
