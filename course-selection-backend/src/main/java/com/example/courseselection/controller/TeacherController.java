package com.example.courseselection.controller;

import com.example.courseselection.common.result.Result;
import com.example.courseselection.entity.CourseSchedule;
import com.example.courseselection.security.CustomUserDetails;
import com.example.courseselection.service.CourseManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teacher")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherController {

    private final CourseManagementService courseManagementService;

    @GetMapping("/my-courses")
    public Result<List<CourseSchedule>> getMyCourses(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(required = false) Long semesterId) {
        
        List<CourseSchedule> schedules = courseManagementService.getTeacherCourseSchedules(
            currentUser.getId(), semesterId);
        
        return Result.success("获取我的课程成功", schedules);
    }
}