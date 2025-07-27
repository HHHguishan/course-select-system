package com.example.courseselection.entity;

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
@Table(name = "grades")
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = false)
public class Grade {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_selection_id", nullable = false)
    private CourseSelection courseSelection;
    
    @Column(name = "regular_score", precision = 5, scale = 2)
    private BigDecimal regularScore;
    
    @Column(name = "midterm_score", precision = 5, scale = 2)
    private BigDecimal midtermScore;
    
    @Column(name = "final_score", precision = 5, scale = 2)
    private BigDecimal finalScore;
    
    @Column(name = "total_score", precision = 5, scale = 2)
    private BigDecimal totalScore;
    
    @Column(name = "grade_point", precision = 3, scale = 2)
    private BigDecimal gradePoint;
    
    @Column(name = "grade_level", length = 10)
    private String gradeLevel;
    
    @Column(name = "is_makeup")
    private Boolean isMakeup = false;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}