package com.example.demo.controller;

import com.example.demo.entity.Course;
import com.example.demo.entity.Student;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.EnrollmentRepository;
import com.example.demo.repository.StudentRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;

    public HomeController(
        CourseRepository courseRepository,
        StudentRepository studentRepository,
        EnrollmentRepository enrollmentRepository
    ) {
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @GetMapping({"/", "/home", "/courses"})
    public String home(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(required = false) String keyword,
        Authentication authentication,
        Model model
    ) {
        int safePage = Math.max(page, 0);
        String normalizedKeyword = keyword == null ? "" : keyword.trim();

        Page<Course> coursePage;
        if (normalizedKeyword.isBlank()) {
            coursePage = courseRepository.findAll(
                PageRequest.of(safePage, 5, Sort.by("id").ascending())
            );
        } else {
            coursePage = courseRepository.findByNameContainingIgnoreCase(
                normalizedKeyword,
                PageRequest.of(safePage, 5, Sort.by("id").ascending())
            );
        }

        if (coursePage.getTotalPages() > 0 && safePage >= coursePage.getTotalPages()) {
            safePage = coursePage.getTotalPages() - 1;
            if (normalizedKeyword.isBlank()) {
                coursePage = courseRepository.findAll(
                    PageRequest.of(safePage, 5, Sort.by("id").ascending())
                );
            } else {
                coursePage = courseRepository.findByNameContainingIgnoreCase(
                    normalizedKeyword,
                    PageRequest.of(safePage, 5, Sort.by("id").ascending())
                );
            }
        }

        List<Integer> pageNumbers = IntStream.range(0, coursePage.getTotalPages())
            .boxed()
            .toList();

        boolean isStudent = authentication != null
            && authentication.getAuthorities().stream().anyMatch(a -> "ROLE_STUDENT".equals(a.getAuthority()));

        Set<Long> enrolledCourseIds = new HashSet<>();
        if (isStudent && authentication != null) {
            Student student = studentRepository.findByUsername(authentication.getName()).orElse(null);
            if (student != null) {
                enrolledCourseIds = new HashSet<>(
                    enrollmentRepository.findEnrolledCourseIdsByStudentId(student.getId())
                );
            }
        }

        model.addAttribute("coursePage", coursePage);
        model.addAttribute("currentPage", safePage);
        model.addAttribute("prevPage", Math.max(safePage - 1, 0));
        model.addAttribute("nextPage", safePage + 1);
        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("isStudent", isStudent);
        model.addAttribute("enrolledCourseIds", enrolledCourseIds);
        model.addAttribute("keyword", normalizedKeyword);
        return "home";
    }
}
