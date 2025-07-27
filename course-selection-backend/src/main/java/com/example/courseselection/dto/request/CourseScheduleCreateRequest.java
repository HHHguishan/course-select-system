package com.example.courseselection.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class CourseScheduleCreateRequest {

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @NotNull(message = "教师ID不能为空")
    private Long teacherId;

    @NotNull(message = "学期ID不能为空")
    private Long semesterId;

    @Size(max = 50, message = "班级名称长度不能超过50个字符")
    private String className;

    @NotNull(message = "最大学生数不能为空")
    @Min(value = 1, message = "最大学生数不能少于1")
    @Max(value = 200, message = "最大学生数不能超过200")
    private Integer maxStudents;

    @Size(max = 50, message = "教室长度不能超过50个字符")
    private String classroom;

    private List<Map<String, Object>> scheduleTime;

    private LocalDateTime selectionStartTime;

    private LocalDateTime selectionEndTime;
}