package com.college.student_app;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StudentController {

    @GetMapping("/student/data")
    public String getStudentData() {
        return "This is STUDENT data.";
    }
}