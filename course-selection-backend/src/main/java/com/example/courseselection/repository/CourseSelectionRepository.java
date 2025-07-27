package com.example.courseselection.repository;

import com.example.courseselection.entity.CourseSelection;
import com.example.courseselection.entity.enums.SelectionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseSelectionRepository extends JpaRepository<CourseSelection, Long> {
    
    List<CourseSelection> findByStudentId(Long studentId);
    
    List<CourseSelection> findByCourseScheduleId(Long courseScheduleId);
    
    Page<CourseSelection> findByStudentIdAndStatus(Long studentId, SelectionStatus status, Pageable pageable);
    
    Optional<CourseSelection> findByStudentIdAndCourseScheduleId(Long studentId, Long courseScheduleId);
    
    boolean existsByStudentIdAndCourseScheduleIdAndStatus(Long studentId, Long courseScheduleId, SelectionStatus status);
    
    @Query("SELECT cs FROM CourseSelection cs WHERE cs.student.id = :studentId " +
           "AND cs.courseSchedule.semester.id = :semesterId " +
           "AND cs.status = :status")
    List<CourseSelection> findByStudentIdAndSemesterIdAndStatus(@Param("studentId") Long studentId,
                                                               @Param("semesterId") Long semesterId,
                                                               @Param("status") SelectionStatus status);
    
    @Query("SELECT SUM(cs.courseSchedule.course.credits) FROM CourseSelection cs " +
           "WHERE cs.student.id = :studentId " +
           "AND cs.courseSchedule.semester.id = :semesterId " +
           "AND cs.status = 'SELECTED'")
    BigDecimal getTotalCreditsByStudentAndSemester(@Param("studentId") Long studentId, 
                                                  @Param("semesterId") Long semesterId);
    
    @Query("SELECT COUNT(cs) FROM CourseSelection cs " +
           "WHERE cs.courseSchedule.id = :courseScheduleId " +
           "AND cs.status = 'SELECTED'")
    Long countSelectedStudentsByCourseSchedule(@Param("courseScheduleId") Long courseScheduleId);
    
    @Query("SELECT cs FROM CourseSelection cs " +
           "WHERE cs.courseSchedule.teacher.id = :teacherId " +
           "AND cs.courseSchedule.semester.id = :semesterId " +
           "AND cs.status = :status")
    List<CourseSelection> findByTeacherIdAndSemesterIdAndStatus(@Param("teacherId") Long teacherId,
                                                               @Param("semesterId") Long semesterId,
                                                               @Param("status") SelectionStatus status);
    
    @Query("SELECT cs FROM CourseSelection cs " +
           "WHERE cs.student.id = :studentId " +
           "AND cs.courseSchedule.course.id IN :courseIds " +
           "AND cs.status = 'SELECTED'")
    List<CourseSelection> findByStudentIdAndCourseIds(@Param("studentId") Long studentId, 
                                                     @Param("courseIds") List<Long> courseIds);
}