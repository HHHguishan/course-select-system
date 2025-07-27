package com.example.courseselection.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourseSelectionRequest {
    
    @NotNull(message = "课程安排ID不能为空")
    private Long courseScheduleId;
}