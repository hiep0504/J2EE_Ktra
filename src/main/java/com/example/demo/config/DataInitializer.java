package com.example.demo.config;

import com.example.demo.entity.Category;
import com.example.demo.entity.Course;
import com.example.demo.entity.Role;
import com.example.demo.entity.Student;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.StudentRepository;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final StudentRepository studentRepository;
    private final CategoryRepository categoryRepository;
    private final CourseRepository courseRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
        RoleRepository roleRepository,
        StudentRepository studentRepository,
        CategoryRepository categoryRepository,
        CourseRepository courseRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.roleRepository = roleRepository;
        this.studentRepository = studentRepository;
        this.categoryRepository = categoryRepository;
        this.courseRepository = courseRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Role adminRole = getOrCreateRole("ADMIN");
        Role studentRole = getOrCreateRole("STUDENT");

        studentRepository.findByUsername("admin").orElseGet(() -> {
            Student admin = new Student();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@example.com");
            admin.setRoles(Set.of(adminRole, studentRole));
            return studentRepository.save(admin);
        });

        Category programming = getOrCreateCategory("Programming");
        Category database = getOrCreateCategory("Database");
        Category network = getOrCreateCategory("Network");

        createCourseIfMissing("Java Core", 3, "Dr. An", "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800", programming);
        createCourseIfMissing("Spring Boot", 3, "Dr. Binh", "https://images.unsplash.com/photo-1555949963-aa79dcee981c?w=800", programming);
        createCourseIfMissing("Web Development", 2, "Ms. Chi", "https://images.unsplash.com/photo-1461749280684-dccba630e2f6?w=800", programming);
        createCourseIfMissing("Database Systems", 3, "Dr. Dung", "https://images.unsplash.com/photo-1544383835-bda2bc66a55d?w=800", database);
        createCourseIfMissing("SQL Practice", 2, "Mr. Giang", "https://images.unsplash.com/photo-1516321497487-e288fb19713f?w=800", database);
        createCourseIfMissing("Computer Networks", 3, "Dr. Huy", "https://images.unsplash.com/photo-1563770660941-10a63607639e?w=800", network);
        createCourseIfMissing("Network Security", 3, "Ms. Khanh", "https://images.unsplash.com/photo-1563013544-824ae1b704d3?w=800", network);
    }

    private Role getOrCreateRole(String roleName) {
        return roleRepository.findByName(roleName)
            .orElseGet(() -> {
                Role role = new Role();
                role.setName(roleName);
                return roleRepository.save(role);
            });
    }

    private Category getOrCreateCategory(String categoryName) {
        return categoryRepository.findByName(categoryName)
            .orElseGet(() -> {
                Category category = new Category();
                category.setName(categoryName);
                return categoryRepository.save(category);
            });
    }

    private void createCourseIfMissing(
        String name,
        Integer credits,
        String lecturer,
        String image,
        Category category
    ) {
        if (courseRepository.existsByName(name)) {
            return;
        }
        courseRepository.save(createCourse(name, credits, lecturer, image, category));
    }

    private Course createCourse(String name, Integer credits, String lecturer, String image, Category category) {
        Course course = new Course();
        course.setName(name);
        course.setCredits(credits);
        course.setLecturer(lecturer);
        course.setImage(image);
        course.setCategory(category);
        return course;
    }
}
