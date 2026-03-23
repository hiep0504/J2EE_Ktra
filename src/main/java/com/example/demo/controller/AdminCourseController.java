package com.example.demo.controller;

import com.example.demo.dto.CourseForm;
import com.example.demo.entity.Category;
import com.example.demo.entity.Course;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.CourseRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/courses")
public class AdminCourseController {

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;

    public AdminCourseController(CourseRepository courseRepository, CategoryRepository categoryRepository) {
        this.courseRepository = courseRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("courses", courseRepository.findAll());
        return "admin/course-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("courseForm", new CourseForm());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("isEdit", false);
        return "admin/course-form";
    }

    @PostMapping("/create")
    public String create(
        @Valid @ModelAttribute("courseForm") CourseForm courseForm,
        BindingResult bindingResult,
        Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("isEdit", false);
            return "admin/course-form";
        }

        Course course = new Course();
        mapFormToCourse(courseForm, course);
        courseRepository.save(course);
        return "redirect:/admin/courses";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Course course = courseRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid course id: " + id));

        CourseForm form = new CourseForm();
        form.setName(course.getName());
        form.setImage(course.getImage());
        form.setCredits(course.getCredits());
        form.setLecturer(course.getLecturer());
        if (course.getCategory() != null) {
            form.setCategoryId(course.getCategory().getId());
        }

        model.addAttribute("course", course);
        model.addAttribute("courseForm", form);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("isEdit", true);
        return "admin/course-form";
    }

    @PostMapping("/edit/{id}")
    public String update(
        @PathVariable Long id,
        @Valid @ModelAttribute("courseForm") CourseForm courseForm,
        BindingResult bindingResult,
        Model model
    ) {
        Course course = courseRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid course id: " + id));

        if (bindingResult.hasErrors()) {
            model.addAttribute("course", course);
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("isEdit", true);
            return "admin/course-form";
        }

        mapFormToCourse(courseForm, course);
        courseRepository.save(course);
        return "redirect:/admin/courses";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        courseRepository.deleteById(id);
        return "redirect:/admin/courses";
    }

    private void mapFormToCourse(CourseForm form, Course course) {
        Category category = categoryRepository.findById(form.getCategoryId())
            .orElseThrow(() -> new IllegalArgumentException("Invalid category id: " + form.getCategoryId()));

        course.setName(form.getName());
        course.setImage(form.getImage());
        course.setCredits(form.getCredits());
        course.setLecturer(form.getLecturer());
        course.setCategory(category);
    }
}
