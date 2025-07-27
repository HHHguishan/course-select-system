package com.example.courseselection.entity;

import com.example.courseselection.entity.enums.ScheduleStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Entity
@Table(name = "course_schedules")
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = false)
public class CourseSchedule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;
    
    @Column(name = "class_name", length = 50)
    private String className;
    
    @Column(name = "max_students", nullable = false)
    private Integer maxStudents;
    
    @Column(name = "current_students")
    private Integer currentStudents = 0;
    
    @Column(name = "classroom", length = 50)
    private String classroom;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "schedule_time", columnDefinition = "JSON")
    private List<Map<String, Object>> scheduleTime;
    
    @Column(name = "selection_start_time")
    private LocalDateTime selectionStartTime;
    
    @Column(name = "selection_end_time")
    private LocalDateTime selectionEndTime;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ScheduleStatus status = ScheduleStatus.PENDING;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "courseSchedule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CourseSelection> courseSelections;
}