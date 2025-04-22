package com.example.english.demo.exception;
//file này cho biết bắt exception nào và trả về nhu nào
import com.example.english.demo.dto.request.ApiResponse;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;
import java.util.Objects;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String MIN_ATTRIBUTE="min";

    //hàm đầu tiên này là ông tổ vì có  Exception.class
    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse> handleingRuntimeException(Exception exception){
        ApiResponse apiResponse=new ApiResponse();

        apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        apiResponse.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());

        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(apiResponse);
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handleingAppException(AppException exception){
        ErrorCode errorCode=exception.getErrorCode();
        ApiResponse apiResponse=new ApiResponse();

        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());

        return ResponseEntity
                .status(errorCode.getHttpStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(apiResponse);
    }

//    nay để xử lý khi chúng ta ko có quyền truy cập và trả về bên phía postman giúp ta dễ nhận diện hơn
    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse> handlingAccessDeniedException(AccessDeniedException exception){
        ErrorCode errorCode=ErrorCode.UNAUTHORIZED;

        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(
                ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handlingValidation(MethodArgumentNotValidException exception){
        String enumkey=exception.getFieldError().getDefaultMessage();

        ErrorCode errorCode=ErrorCode.INVALID_KEY;
        Map<String,Objects> attributes=null;

        try{
            errorCode=ErrorCode.valueOf(enumkey);

            var constraintValidation=exception.getBindingResult()
                    .getAllErrors().get(0).unwrap(ConstraintViolation.class);// nếu chúng ta muonos get tất cả thay vì get 1 cái thì chúng ta sẽ loop thay vì getfirst

            attributes=constraintValidation.getConstraintDescriptor().getAttributes();

            log.info(attributes.toString());
        } catch (IllegalArgumentException exception1){

        }
        ApiResponse apiResponse=new ApiResponse();

        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(Objects.nonNull(attributes)?
                mapAttribute(errorCode.getMessage(),attributes)
                : errorCode.getMessage());

        return ResponseEntity.badRequest().body(apiResponse);
    }

    private String mapAttribute(String message, Map<String, Objects>attributes){
        String minValue = String.valueOf(attributes.get(MIN_ATTRIBUTE));

        return message.replace("{"+MIN_ATTRIBUTE+"}",minValue);//nếu không tìm thấy thì thôi
    }


}
