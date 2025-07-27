package com.example.courseselection.repository;

import com.example.courseselection.entity.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    
    Optional<Teacher> findByTeacherNumber(String teacherNumber);
    
    Optional<Teacher> findByUserId(Long userId);
    
    boolean existsByTeacherNumber(String teacherNumber);
    
    List<Teacher> findByDepartment(String department);
    
    List<Teacher> findByTitle(String title);
    
    Page<Teacher> findByDepartmentAndTitle(String department, String title, Pageable pageable);
    
    @Query("SELECT t FROM Teacher t WHERE t.user.realName LIKE %:name%")
    List<Teacher> findByUserRealNameContaining(@Param("name") String name);
    
    @Query("SELECT t FROM Teacher t WHERE t.department = :department AND t.title = :title")
    List<Teacher> findByDepartmentAndTitleExact(@Param("department") String department, 
                                               @Param("title") String title);
}