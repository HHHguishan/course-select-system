package com.example.courseselection.repository;

import com.example.courseselection.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    Optional<Student> findByStudentNumber(String studentNumber);
    
    Optional<Student> findByUserId(Long userId);
    
    boolean existsByStudentNumber(String studentNumber);
    
    List<Student> findByGrade(String grade);
    
    List<Student> findByMajor(String major);
    
    List<Student> findByClassName(String className);
    
    Page<Student> findByGradeAndMajor(String grade, String major, Pageable pageable);
    
    @Query("SELECT s FROM Student s WHERE s.user.realName LIKE %:name%")
    List<Student> findByUserRealNameContaining(@Param("name") String name);
    
    @Query("SELECT s FROM Student s WHERE s.grade = :grade AND s.major = :major AND s.className = :className")
    List<Student> findByGradeAndMajorAndClassName(@Param("grade") String grade, 
                                                  @Param("major") String major, 
                                                  @Param("className") String className);
}