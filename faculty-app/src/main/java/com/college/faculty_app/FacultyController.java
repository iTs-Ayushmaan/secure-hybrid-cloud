package com.college.faculty_app;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FacultyController {

    @GetMapping("/faculty/data")
    public String getFacultyData() {
        return "This is FACULTY data.";
    }
}