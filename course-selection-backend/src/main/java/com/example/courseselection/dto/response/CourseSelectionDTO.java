package com.example.courseselection.dto.response;

import com.example.courseselection.entity.enums.CourseType;
import com.example.courseselection.entity.enums.SelectionStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class CourseSelectionDTO {
    
    private Long id;
    private LocalDateTime selectionTime;
    private SelectionStatus status;
    private BigDecimal grade;
    private BigDecimal gradePoint;
    private Boolean isPassed;
    
    // 课程安排信息
    private Long courseScheduleId;
    private String className;
    private String classroom;
    private List<Map<String, Object>> scheduleTime;
    
    // 课程信息
    private Long courseId;
    private String courseCode;
    private String courseName;
    private CourseType courseType;
    private BigDecimal credits;
    private Integer totalHours;
    private String description;
    
    // 教师信息
    private Long teacherId;
    private String teacherName;
    private String teacherTitle;
    
    // 学期信息
    private Long semesterId;
    private String semesterName;
    
    // 学生信息（教师查看时使用）
    private Long studentId;
    private String studentNumber;
    private String studentName;
    private String studentMajor;
    private String studentClass;
}