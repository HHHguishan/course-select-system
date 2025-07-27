package com.example.courseselection.entity;

import com.example.courseselection.entity.enums.CourseStatus;
import com.example.courseselection.entity.enums.CourseType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "courses")
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = false)
public class Course {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "course_code", unique = true, nullable = false, length = 20)
    private String courseCode;
    
    @Column(name = "course_name", nullable = false, length = 100)
    private String courseName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "course_type", nullable = false)
    private CourseType courseType;
    
    @Column(name = "credits", nullable = false, precision = 3, scale = 1)
    private BigDecimal credits;
    
    @Column(name = "total_hours", nullable = false)
    private Integer totalHours;
    
    @Column(name = "theory_hours")
    private Integer theoryHours = 0;
    
    @Column(name = "practice_hours")
    private Integer practiceHours = 0;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "prerequisite_courses", columnDefinition = "JSON")
    private List<Long> prerequisiteCourses;
    
    @Column(name = "department", length = 100)
    private String department;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private CourseStatus status = CourseStatus.ACTIVE;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CourseSchedule> courseSchedules;
}