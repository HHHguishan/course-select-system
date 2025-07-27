package com.example.courseselection.repository;

import com.example.courseselection.entity.CourseSchedule;
import com.example.courseselection.entity.enums.ScheduleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseScheduleRepository extends JpaRepository<CourseSchedule, Long> {
    
    List<CourseSchedule> findBySemesterId(Long semesterId);
    
    List<CourseSchedule> findByTeacherId(Long teacherId);
    
    List<CourseSchedule> findByCourseId(Long courseId);
    
    List<CourseSchedule> findByStatus(ScheduleStatus status);
    
    Page<CourseSchedule> findBySemesterIdAndStatus(Long semesterId, ScheduleStatus status, Pageable pageable);
    
    @Query("SELECT cs FROM CourseSchedule cs WHERE cs.semester.id = :semesterId " +
           "AND cs.status = 'OPEN' " +
           "AND cs.selectionStartTime <= :now " +
           "AND cs.selectionEndTime >= :now")
    List<CourseSchedule> findAvailableCoursesForSelection(@Param("semesterId") Long semesterId, 
                                                         @Param("now") LocalDateTime now);
    
    @Query("SELECT cs FROM CourseSchedule cs WHERE cs.teacher.id = :teacherId AND cs.semester.id = :semesterId")
    List<CourseSchedule> findByTeacherIdAndSemesterId(@Param("teacherId") Long teacherId, 
                                                     @Param("semesterId") Long semesterId);
    
    @Query("SELECT cs FROM CourseSchedule cs WHERE " +
           "cs.semester.id = :semesterId AND " +
           "(:courseId IS NULL OR cs.course.id = :courseId) AND " +
           "(:teacherId IS NULL OR cs.teacher.id = :teacherId) AND " +
           "(:status IS NULL OR cs.status = :status)")
    Page<CourseSchedule> findCourseSchedulesWithFilters(@Param("semesterId") Long semesterId,
                                                       @Param("courseId") Long courseId,
                                                       @Param("teacherId") Long teacherId,
                                                       @Param("status") ScheduleStatus status,
                                                       Pageable pageable);
}