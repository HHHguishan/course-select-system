package com.example.courseselection.controller;

import com.example.courseselection.common.result.Result;
import com.example.courseselection.dto.request.CourseSelectionRequest;
import com.example.courseselection.dto.response.CourseScheduleDTO;
import com.example.courseselection.dto.response.CourseSelectionDTO;
import com.example.courseselection.security.CustomUserDetails;
import com.example.courseselection.service.CourseSelectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/course-selection")
@RequiredArgsConstructor
public class CourseSelectionController {

    private final CourseSelectionService courseSelectionService;

    @GetMapping("/available")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<Page<CourseScheduleDTO>> getAvailableCourses(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String keyword) {
        
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) 
            ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        Page<CourseScheduleDTO> courses = courseSelectionService.getAvailableCourses(
            currentUser.getId(), pageable, keyword);
        
        return Result.success("获取可选课程成功", courses);
    }

    @PostMapping("/select")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<Void> selectCourse(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody CourseSelectionRequest request) {
        
        courseSelectionService.selectCourse(currentUser.getId(), request.getCourseScheduleId());
        return Result.<Void>success("选课成功", null);
    }

    @PostMapping("/drop/{courseScheduleId}")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<Void> dropCourse(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long courseScheduleId) {
        
        courseSelectionService.dropCourse(currentUser.getId(), courseScheduleId);
        return Result.<Void>success("退课成功", null);
    }

    @GetMapping("/my-courses")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<Page<CourseSelectionDTO>> getMySelectedCourses(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "selectionTime") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) 
            ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        Page<CourseSelectionDTO> selections = courseSelectionService.getMySelectedCourses(
            currentUser.getId(), pageable);
        
        return Result.success("获取已选课程成功", selections);
    }

    @GetMapping("/course/{courseScheduleId}/students")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public Result<Page<CourseSelectionDTO>> getStudentsByCourseSchedule(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long courseScheduleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "selectionTime") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) 
            ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        Page<CourseSelectionDTO> students = courseSelectionService.getStudentsByCourseSchedule(
            currentUser.getId(), courseScheduleId, pageable);
        
        return Result.success("获取选课学生名单成功", students);
    }
}