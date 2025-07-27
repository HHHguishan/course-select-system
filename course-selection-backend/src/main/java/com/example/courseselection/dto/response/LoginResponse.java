package com.example.courseselection.dto.response;

import com.example.courseselection.entity.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserInfo userInfo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String realName;
        private UserRole role;
        
        // 学生信息
        private String studentNumber;
        private String grade;
        private String major;
        private String className;
        
        // 教师信息
        private String teacherNumber;
        private String department;
        private String title;
    }

    public LoginResponse(String accessToken, String refreshToken, Long expiresIn, UserInfo userInfo) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.userInfo = userInfo;
    }
}