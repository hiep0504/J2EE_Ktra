package com.example.demo.service;

import com.example.demo.dto.RegistrationForm;
import com.example.demo.entity.Role;
import com.example.demo.entity.Student;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.StudentRepository;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(
        StudentRepository studentRepository,
        RoleRepository roleRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.studentRepository = studentRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String registerStudent(RegistrationForm form) {
        if (studentRepository.existsByUsername(form.getUsername())) {
            return "Username already exists";
        }

        if (studentRepository.existsByEmail(form.getEmail())) {
            return "Email already exists";
        }

        Role studentRole = roleRepository.findByName("STUDENT")
            .orElseThrow(() -> new IllegalStateException("Default role STUDENT not found"));

        Student student = new Student();
        student.setUsername(form.getUsername());
        student.setPassword(passwordEncoder.encode(form.getPassword()));
        student.setEmail(form.getEmail());
        student.setRoles(Set.of(studentRole));

        studentRepository.save(student);
        return null;
    }
}
