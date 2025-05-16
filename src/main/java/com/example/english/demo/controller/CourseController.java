package com.example.english.demo.controller;

import com.example.english.demo.dto.response.CourseResponseDTO;
import com.example.english.demo.entity.Course;
import com.example.english.demo.repository.CourseRepository;
import com.example.english.demo.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/course")
public class CourseController {

    private final CourseService courseService;

    private final CourseRepository courseRepository;

    @GetMapping("/list")
    public String listCourses(Model model) {
        List<CourseResponseDTO> courses = courseService.getAllCourses();
        model.addAttribute("courses", courses);
        return "listCourse";
    }

    @GetMapping("/detail/{id}")
    public String getCourseDetail(@PathVariable Long id, Model model) {
        Course course = courseRepository.findById(id).orElse(null);
        if (course == null) {
            return "redirect:/error";
        }

        model.addAttribute("course", course);
        return "courseDetail";
    }

}
