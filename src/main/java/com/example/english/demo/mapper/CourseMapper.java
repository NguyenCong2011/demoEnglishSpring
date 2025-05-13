package com.example.english.demo.mapper;

import com.example.english.demo.dto.request.CourseRequestDTO;
import com.example.english.demo.dto.response.CourseResponseDTO;
import com.example.english.demo.entity.Course;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CourseMapper {

    Course toCourse(CourseRequestDTO courseRequestDTO);

    CourseResponseDTO toCourseResponse(Course course);

    void updateCourse(@MappingTarget Course course, CourseRequestDTO requestDTO);
}
