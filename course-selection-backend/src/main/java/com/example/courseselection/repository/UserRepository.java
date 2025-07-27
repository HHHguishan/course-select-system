package com.example.courseselection.repository;

import com.example.courseselection.entity.User;
import com.example.courseselection.entity.enums.UserRole;
import com.example.courseselection.entity.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    List<User> findByRole(UserRole role);
    
    List<User> findByStatus(UserStatus status);
    
    Page<User> findByRoleAndStatus(UserRole role, UserStatus status, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.realName LIKE %:name% AND u.role = :role")
    List<User> findByRealNameContainingAndRole(@Param("name") String name, @Param("role") UserRole role);
}