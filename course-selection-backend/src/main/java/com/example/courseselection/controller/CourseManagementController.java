package com.example.courseselection.controller;

import com.example.courseselection.common.result.Result;
import com.example.courseselection.dto.request.CourseCreateRequest;
import com.example.courseselection.dto.request.CourseScheduleCreateRequest;
import com.example.courseselection.dto.response.CourseScheduleDTO;
import com.example.courseselection.entity.Course;
import com.example.courseselection.entity.CourseSchedule;
import com.example.courseselection.entity.enums.ScheduleStatus;
import com.example.courseselection.service.CourseManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/course-management")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CourseManagementController {

    private final CourseManagementService courseManagementService;

    @PostMapping("/courses")
    public Result<Course> createCourse(@Valid @RequestBody CourseCreateRequest request) {
        Course course = courseManagementService.createCourse(request);
        return Result.success("创建课程成功", course);
    }

    @GetMapping("/courses")
    public Result<Page<Course>> getAllCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) 
            ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        Page<Course> courses = courseManagementService.getAllCourses(pageable);
        return Result.success("获取课程列表成功", courses);
    }

    @DeleteMapping("/courses/{courseId}")
    public Result<Void> deleteCourse(@PathVariable Long courseId) {
        courseManagementService.deleteCourse(courseId);
        return Result.<Void>success("删除课程成功", null);
    }

    @PostMapping("/course-schedules")
    public Result<CourseSchedule> createCourseSchedule(@Valid @RequestBody CourseScheduleCreateRequest request) {
        CourseSchedule schedule = courseManagementService.createCourseSchedule(request);
        return Result.success("创建课程安排成功", schedule);
    }

    @GetMapping("/course-schedules")
    public Result<Page<CourseScheduleDTO>> getCourseSchedules(
            @RequestParam(required = false) Long semesterId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) 
            ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        Page<CourseScheduleDTO> schedules = courseManagementService.getCourseSchedules(semesterId, pageable);
        return Result.success("获取课程安排列表成功", schedules);
    }

    @PutMapping("/course-schedules/{scheduleId}/status")
    public Result<Void> updateCourseScheduleStatus(
            @PathVariable Long scheduleId,
            @RequestParam ScheduleStatus status) {
        
        courseManagementService.updateCourseScheduleStatus(scheduleId, status);
        return Result.<Void>success("更新课程安排状态成功", null);
    }

    @DeleteMapping("/course-schedules/{scheduleId}")
    public Result<Void> deleteCourseSchedule(@PathVariable Long scheduleId) {
        courseManagementService.deleteCourseSchedule(scheduleId);
        return Result.<Void>success("删除课程安排成功", null);
    }
}