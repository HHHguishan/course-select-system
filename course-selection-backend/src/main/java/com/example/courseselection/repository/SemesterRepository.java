package com.example.courseselection.repository;

import com.example.courseselection.entity.Semester;
import com.example.courseselection.entity.enums.SemesterType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, Long> {
    
    Optional<Semester> findByIsCurrent(Boolean isCurrent);
    
    @Query("SELECT s FROM Semester s WHERE s.isCurrent = true")
    Optional<Semester> findCurrentSemester();
    
    List<Semester> findByAcademicYear(String academicYear);
    
    List<Semester> findBySemesterType(SemesterType semesterType);
    
    List<Semester> findByAcademicYearAndSemesterType(String academicYear, SemesterType semesterType);
    
    @Query("SELECT s FROM Semester s ORDER BY s.startDate DESC")
    List<Semester> findAllOrderByStartDateDesc();
}