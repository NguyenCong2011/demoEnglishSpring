package com.example.english.demo.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(999999,"Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),//500 nếu không có loại nào catch đk thì trả v 9999
    USER_EXITSTED(10115,"User existeddd",HttpStatus.BAD_REQUEST),
    USER_NOT_EXITSTED(1011555,"User  not existeddd",HttpStatus.NOT_FOUND),
    USERNAME_INVALID(1111,"UserName must be at least {min} character",HttpStatus.BAD_REQUEST),/// ví dụ cho phần message trong phần xác thực dữ liệu đầu vào ý ,truyền cái ny vào trong cái nào tạo
    INVALID_PASWORD(1111111,"password must be {min} character",HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(2222222,"Usser Unauthenticated",HttpStatus.UNAUTHORIZED),
    INVALID_KEY(1144444,"mày nhập sai các key ngay bên trên",HttpStatus.BAD_REQUEST),//dùng tránh nhập sai key bên dùng món trên
    UNAUTHORIZED(22222223,"you have not permission",HttpStatus.FORBIDDEN),
    INVALID_DOB(15555,"Your dob must at least {min}",HttpStatus.BAD_REQUEST),
    TOEIC_EXAM_EXITSTED(155575,"Exxam existed",HttpStatus.BAD_REQUEST),
    TOEIC_EXAM_NOT_EXITSTED(1555875,"Exxam not existed",HttpStatus.BAD_REQUEST),
    FILE_UPLOAD_ERROR(1565435875,"file not = not existed",HttpStatus.BAD_REQUEST),
    TOEIC_QUESTION_EXITSTED(1555775,"Question existed",HttpStatus.BAD_REQUEST),
    TOEIC_QUESTION_NOT_EXITSTED(1555775,"Question not existed",HttpStatus.BAD_REQUEST);


    private int code;
    private String message;
    private HttpStatusCode httpStatusCode;


    ErrorCode(int code, String message,HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode=httpStatusCode;
    }
}
