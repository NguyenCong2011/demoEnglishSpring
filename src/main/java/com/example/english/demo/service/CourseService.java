package com.example.english.demo.service;

import com.example.english.demo.dto.request.CourseRequestDTO;
import com.example.english.demo.dto.response.CourseResponseDTO;
import com.example.english.demo.entity.Course;
import com.example.english.demo.mapper.CourseMapper;
import com.example.english.demo.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;

    public CourseResponseDTO createCourse(CourseRequestDTO requestDTO) {
        Course course = courseMapper.toCourse(requestDTO);
        Course saved = courseRepository.save(course);
        return courseMapper.toCourseResponse(saved);
    }

    public List<CourseResponseDTO> getAllCourses() {
        List<Course> courses = courseRepository.findAll();
        System.out.println("Courses in DB: " + courses.size());
        return courses.stream()
                .map(courseMapper::toCourseResponse)
                .collect(Collectors.toList());
    }


    public CourseResponseDTO getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Course not found"));
        return courseMapper.toCourseResponse(course);
    }

    public CourseResponseDTO updateCourse(Long id, CourseRequestDTO requestDTO) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Course not found"));

        courseMapper.updateCourse(course, requestDTO);
        Course updated = courseRepository.save(course);

        return courseMapper.toCourseResponse(updated);
    }

    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResponseStatusException(NOT_FOUND, "Course not found");
        }
        courseRepository.deleteById(id);
    }
}
