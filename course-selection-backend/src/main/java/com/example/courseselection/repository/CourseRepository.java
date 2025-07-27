package com.example.courseselection.repository;

import com.example.courseselection.entity.Course;
import com.example.courseselection.entity.enums.CourseStatus;
import com.example.courseselection.entity.enums.CourseType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    Optional<Course> findByCourseCode(String courseCode);
    
    boolean existsByCourseCode(String courseCode);
    
    List<Course> findByStatus(CourseStatus status);
    
    Page<Course> findByStatus(CourseStatus status, Pageable pageable);
    
    List<Course> findByCourseType(CourseType courseType);
    
    List<Course> findByDepartment(String department);
    
    Page<Course> findByStatusAndCourseType(CourseStatus status, CourseType courseType, Pageable pageable);
    
    @Query("SELECT c FROM Course c WHERE c.courseName LIKE %:name% AND c.status = :status")
    List<Course> findByCourseNameContainingAndStatus(@Param("name") String name, @Param("status") CourseStatus status);
    
    @Query("SELECT c FROM Course c WHERE c.department = :department AND c.courseType = :type AND c.status = :status")
    Page<Course> findByDepartmentAndCourseTypeAndStatus(@Param("department") String department, 
                                                       @Param("type") CourseType type, 
                                                       @Param("status") CourseStatus status, 
                                                       Pageable pageable);
    
    @Query("SELECT c FROM Course c WHERE " +
           "(:name IS NULL OR c.courseName LIKE %:name%) AND " +
           "(:department IS NULL OR c.department = :department) AND " +
           "(:courseType IS NULL OR c.courseType = :courseType) AND " +
           "c.status = :status")
    Page<Course> findCoursesWithFilters(@Param("name") String name,
                                       @Param("department") String department,
                                       @Param("courseType") CourseType courseType,
                                       @Param("status") CourseStatus status,
                                       Pageable pageable);
}