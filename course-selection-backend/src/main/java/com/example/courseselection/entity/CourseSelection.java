package com.example.courseselection.entity;

import com.example.courseselection.entity.enums.SelectionStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "course_selections", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "course_schedule_id"}))
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = false)
public class CourseSelection {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_schedule_id", nullable = false)
    private CourseSchedule courseSchedule;
    
    @Column(name = "selection_time")
    private LocalDateTime selectionTime;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SelectionStatus status = SelectionStatus.SELECTED;
    
    @Column(name = "grade", precision = 5, scale = 2)
    private BigDecimal grade;
    
    @Column(name = "grade_point", precision = 3, scale = 2)
    private BigDecimal gradePoint;
    
    @Column(name = "is_passed")
    private Boolean isPassed;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToOne(mappedBy = "courseSelection", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Grade gradeDetail;
}