package com.example.english.demo.dto.request;
//cái này dùng để chuẩn hóa cái api trả về theo dạng nào (nomarrlize)
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)//set những file nào null thì chúng ta không hiển thị ở phần response
public class ApiResponse<T>{
    @Builder.Default
    private int code=1000;
    private String message="ok rồi";
    private T result;
}
