package com.example.courseselection.service;

import com.example.courseselection.common.exception.BusinessException;
import com.example.courseselection.common.result.ResultCode;
import com.example.courseselection.dto.request.LoginRequest;
import com.example.courseselection.dto.request.RegisterRequest;
import com.example.courseselection.dto.response.LoginResponse;
import com.example.courseselection.entity.Student;
import com.example.courseselection.entity.Teacher;
import com.example.courseselection.entity.User;
import com.example.courseselection.entity.enums.UserRole;
import com.example.courseselection.entity.enums.UserStatus;
import com.example.courseselection.repository.StudentRepository;
import com.example.courseselection.repository.TeacherRepository;
import com.example.courseselection.repository.UserRepository;
import com.example.courseselection.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 验证用户名和密码
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 获取用户信息
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        // 检查用户状态
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 生成token
        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(user.getId());

        // 构建用户信息
        LoginResponse.UserInfo userInfo = buildUserInfo(user);

        log.info("User {} logged in successfully", user.getUsername());
        return new LoginResponse(accessToken, refreshToken, jwtExpiration / 1000, userInfo);
    }

    @Transactional
    public void register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS, "用户名已存在");
        }

        // 检查邮箱是否已存在
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS, "邮箱已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRealName(request.getRealName());
        user.setGender(request.getGender());
        user.setRole(request.getRole());
        user.setStatus(UserStatus.ACTIVE);

        user = userRepository.save(user);

        // 根据角色创建对应的扩展信息
        if (request.getRole() == UserRole.STUDENT) {
            createStudentProfile(user, request);
        } else if (request.getRole() == UserRole.TEACHER) {
            createTeacherProfile(user, request);
        }

        log.info("User {} registered successfully with role {}", user.getUsername(), user.getRole());
    }

    public LoginResponse refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken) || !tokenProvider.isRefreshToken(refreshToken)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID, "无效的刷新令牌");
        }

        Long userId = tokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 生成新的访问令牌
        String newAccessToken = tokenProvider.generateToken(
                user.getId(), user.getUsername(), user.getRole(), user.getRealName()
        );
        String newRefreshToken = tokenProvider.generateRefreshToken(user.getId());

        LoginResponse.UserInfo userInfo = buildUserInfo(user);

        return new LoginResponse(newAccessToken, newRefreshToken, jwtExpiration / 1000, userInfo);
    }

    private void createStudentProfile(User user, RegisterRequest request) {
        // 检查学号是否已存在
        if (request.getStudentNumber() != null && 
            studentRepository.existsByStudentNumber(request.getStudentNumber())) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS, "学号已存在");
        }

        Student student = new Student();
        student.setUser(user);
        student.setStudentNumber(request.getStudentNumber());
        student.setGrade(request.getGrade());
        student.setMajor(request.getMajor());
        student.setClassName(request.getClassName());
        student.setEnrollmentYear(request.getEnrollmentYear());
        student.setMaxCredits(30); // 默认最大学分

        studentRepository.save(student);
    }

    private void createTeacherProfile(User user, RegisterRequest request) {
        // 检查工号是否已存在
        if (request.getTeacherNumber() != null && 
            teacherRepository.existsByTeacherNumber(request.getTeacherNumber())) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS, "工号已存在");
        }

        Teacher teacher = new Teacher();
        teacher.setUser(user);
        teacher.setTeacherNumber(request.getTeacherNumber());
        teacher.setDepartment(request.getDepartment());
        teacher.setTitle(request.getTitle());
        teacher.setResearchDirection(request.getResearchDirection());
        teacher.setOfficeLocation(request.getOfficeLocation());

        teacherRepository.save(teacher);
    }

    private LoginResponse.UserInfo buildUserInfo(User user) {
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setEmail(user.getEmail());
        userInfo.setRealName(user.getRealName());
        userInfo.setRole(user.getRole());

        // 根据角色设置扩展信息
        if (user.getRole() == UserRole.STUDENT) {
            studentRepository.findByUserId(user.getId()).ifPresent(student -> {
                userInfo.setStudentNumber(student.getStudentNumber());
                userInfo.setGrade(student.getGrade());
                userInfo.setMajor(student.getMajor());
                userInfo.setClassName(student.getClassName());
            });
        } else if (user.getRole() == UserRole.TEACHER) {
            teacherRepository.findByUserId(user.getId()).ifPresent(teacher -> {
                userInfo.setTeacherNumber(teacher.getTeacherNumber());
                userInfo.setDepartment(teacher.getDepartment());
                userInfo.setTitle(teacher.getTitle());
            });
        }

        return userInfo;
    }
}