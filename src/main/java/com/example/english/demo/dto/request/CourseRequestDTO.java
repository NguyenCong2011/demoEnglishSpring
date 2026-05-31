package com.example.english.demo.dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseRequestDTO {
    private String courseName;
    private String description;
    private Double price;
    private Integer duration;
    private Boolean isActive;
}
