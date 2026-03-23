package com.example.demo.controller;

import com.example.demo.entity.Course;
import com.example.demo.entity.Enrollment;
import com.example.demo.entity.Student;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.EnrollmentRepository;
import com.example.demo.repository.StudentRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/enroll")
public class EnrollmentController {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public EnrollmentController(
        EnrollmentRepository enrollmentRepository,
        StudentRepository studentRepository,
        CourseRepository courseRepository
    ) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    @PostMapping("/{courseId}")
    public String enrollCourse(
        @PathVariable Long courseId,
        Authentication authentication,
        RedirectAttributes redirectAttributes
    ) {
        Student student = studentRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            redirectAttributes.addFlashAttribute("enrollMessage", "You already enrolled in this course.");
            return "redirect:/courses";
        }

        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid course id: " + courseId));

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setEnrollDate(LocalDate.now());
        enrollmentRepository.save(enrollment);

        redirectAttributes.addFlashAttribute("enrollMessage", "Enroll successful.");
        return "redirect:/courses";
    }

    @GetMapping("/my-courses")
    public String myCourses(Authentication authentication, Model model) {
        Student student = studentRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        List<Enrollment> enrollments = enrollmentRepository.findMyEnrollments(student.getId());
        model.addAttribute("enrollments", enrollments);
        return "my-courses";
    }
}
