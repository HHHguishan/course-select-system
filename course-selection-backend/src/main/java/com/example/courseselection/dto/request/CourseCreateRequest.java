package com.example.courseselection.dto.request;

import com.example.courseselection.entity.enums.CourseType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CourseCreateRequest {

    @NotBlank(message = "课程代码不能为空")
    @Pattern(regexp = "^[A-Z0-9]{4,10}$", message = "课程代码格式不正确")
    private String courseCode;

    @NotBlank(message = "课程名称不能为空")
    @Size(max = 100, message = "课程名称长度不能超过100个字符")
    private String courseName;

    @NotNull(message = "课程类型不能为空")
    private CourseType courseType;

    @NotNull(message = "学分不能为空")
    @DecimalMin(value = "0.5", message = "学分不能少于0.5")
    @DecimalMax(value = "10.0", message = "学分不能超过10.0")
    private BigDecimal credits;

    @NotNull(message = "总学时不能为空")
    @Min(value = 1, message = "总学时不能少于1")
    @Max(value = 200, message = "总学时不能超过200")
    private Integer totalHours;

    @Min(value = 0, message = "理论学时不能为负数")
    private Integer theoryHours = 0;

    @Min(value = 0, message = "实践学时不能为负数")
    private Integer practiceHours = 0;

    @Size(max = 1000, message = "课程描述长度不能超过1000个字符")
    private String description;

    private List<Long> prerequisiteCourses;

    @Size(max = 100, message = "开课院系长度不能超过100个字符")
    private String department;
}