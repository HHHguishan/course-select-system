package com.example.courseselection.dto.response;

import com.example.courseselection.entity.enums.CourseType;
import com.example.courseselection.entity.enums.ScheduleStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class CourseScheduleDTO {
    
    private Long id;
    private String className;
    private Integer maxStudents;
    private Integer currentStudents;
    private String classroom;
    private List<Map<String, Object>> scheduleTime;
    private LocalDateTime selectionStartTime;
    private LocalDateTime selectionEndTime;
    private ScheduleStatus status;
    
    // 课程信息
    private Long courseId;
    private String courseCode;
    private String courseName;
    private CourseType courseType;
    private BigDecimal credits;
    private Integer totalHours;
    private String description;
    private String department;
    
    // 教师信息
    private Long teacherId;
    private String teacherName;
    private String teacherTitle;
    private String teacherDepartment;
    
    // 学期信息
    private Long semesterId;
    private String semesterName;
    
    // 选课状态（对学生显示）
    private Boolean isSelected;
    private Boolean canSelect;
    private String selectionMessage;
}