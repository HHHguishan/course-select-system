package com.example.courseselection.dto.request;

import com.example.courseselection.entity.enums.Gender;
import com.example.courseselection.entity.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = "用户名只能包含字母、数字、下划线，长度4-20位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,20}$", 
             message = "密码必须包含大小写字母和数字，长度8-20位")
    private String password;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    private Gender gender;

    @NotNull(message = "用户角色不能为空")
    private UserRole role;

    // 学生特有字段
    private String studentNumber;
    private String grade;
    private String major;
    private String className;
    private Integer enrollmentYear;

    // 教师特有字段
    private String teacherNumber;
    private String department;
    private String title;
    private String researchDirection;
    private String officeLocation;
}