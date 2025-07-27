package com.example.courseselection.config;

import com.example.courseselection.entity.*;
import com.example.courseselection.entity.enums.*;
import com.example.courseselection.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;
    private final CourseScheduleRepository courseScheduleRepository;
    private final SemesterRepository semesterRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 检查是否已经初始化过数据
        if (userRepository.count() > 0) {
            log.info("数据已存在，跳过初始化");
            return;
        }
        
        log.info("开始初始化测试数据...");
        
        // 创建学期
        Semester currentSemester = createSemester();
        
        // 创建用户和角色
        createUsers(currentSemester);
        
        // 创建课程
        createCourses(currentSemester);
        
        log.info("测试数据初始化完成");
    }
    
    private Semester createSemester() {
        Semester semester = new Semester();
        semester.setName("2025春季学期");
        semester.setAcademicYear("2024-2025");
        semester.setSemesterType(SemesterType.SPRING);
        LocalDateTime now = LocalDateTime.now();
        semester.setStartDate(now.toLocalDate().minusMonths(1)); // 一个月前开始
        semester.setEndDate(now.toLocalDate().plusMonths(4)); // 四个月后结束
        semester.setIsCurrent(true);
        return semesterRepository.save(semester);
    }
    
    private void createUsers(Semester semester) {
        // 创建管理员
        User adminUser = createUser("admin", "管理员", "admin@example.com", UserRole.ADMIN);
        
        // 创建教师
        User teacher1User = createUser("teacher1", "张教授", "zhang@example.com", UserRole.TEACHER);
        User teacher2User = createUser("teacher2", "李教授", "li@example.com", UserRole.TEACHER);
        
        Teacher teacher1 = createTeacher(teacher1User, "T001", "计算机学院");
        Teacher teacher2 = createTeacher(teacher2User, "T002", "数学学院");
        
        // 创建学生
        User student1User = createUser("student1", "王同学", "wang@example.com", UserRole.STUDENT);
        User student2User = createUser("student2", "陈同学", "chen@example.com", UserRole.STUDENT);
        User student3User = createUser("student3", "刘同学", "liu@example.com", UserRole.STUDENT);
        
        createStudent(student1User, "2024001", "计算机科学与技术", 2024);
        createStudent(student2User, "2024002", "软件工程", 2024);
        createStudent(student3User, "2024003", "数据科学", 2024);
    }
    
    private User createUser(String username, String realName, String email, UserRole role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("123456"));
        user.setRealName(realName);
        user.setEmail(email);
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
    
    private Teacher createTeacher(User user, String teacherNumber, String department) {
        Teacher teacher = new Teacher();
        teacher.setUser(user);
        teacher.setTeacherNumber(teacherNumber);
        teacher.setDepartment(department);
        teacher.setCreatedAt(LocalDateTime.now());
        return teacherRepository.save(teacher);
    }
    
    private Student createStudent(User user, String studentNumber, String major, int enrollmentYear) {
        Student student = new Student();
        student.setUser(user);
        student.setStudentNumber(studentNumber);
        student.setMajor(major);
        student.setEnrollmentYear(enrollmentYear);
        student.setCreatedAt(LocalDateTime.now());
        return studentRepository.save(student);
    }
    
    private void createCourses(Semester semester) {
        // 获取教师
        Teacher teacher1 = teacherRepository.findByTeacherNumber("T001").orElseThrow();
        Teacher teacher2 = teacherRepository.findByTeacherNumber("T002").orElseThrow();
        
        // 创建课程
        Course course1 = createCourse("CS101", "计算机科学导论", 3, "计算机科学基础课程，介绍计算机科学的基本概念和原理。");
        Course course2 = createCourse("CS102", "程序设计基础", 4, "学习程序设计的基本方法和技巧，掌握一门编程语言。");
        Course course3 = createCourse("MATH201", "高等数学", 4, "数学分析的基础课程，包括极限、导数、积分等内容。");
        Course course4 = createCourse("CS201", "数据结构与算法", 3, "学习基本的数据结构和算法设计与分析方法。");
        Course course5 = createCourse("CS301", "数据库系统原理", 3, "数据库系统的设计原理和实现技术。");
        
        // 创建课程安排
        createCourseSchedule(course1, teacher1, semester, "A101", 1, LocalTime.of(8, 0), LocalTime.of(9, 40), 50);
        createCourseSchedule(course2, teacher1, semester, "A102", 2, LocalTime.of(10, 0), LocalTime.of(11, 40), 40);
        createCourseSchedule(course3, teacher2, semester, "B101", 3, LocalTime.of(14, 0), LocalTime.of(15, 40), 60);
        createCourseSchedule(course4, teacher1, semester, "A103", 4, LocalTime.of(16, 0), LocalTime.of(17, 40), 35);
        createCourseSchedule(course5, teacher1, semester, "A104", 5, LocalTime.of(19, 0), LocalTime.of(20, 40), 30);
    }
    
    private Course createCourse(String courseCode, String courseName, int credits, String description) {
        Course course = new Course();
        course.setCourseCode(courseCode);
        course.setCourseName(courseName);
        course.setCourseType(CourseType.ELECTIVE); // 设置课程类型
        course.setCredits(BigDecimal.valueOf(credits));
        course.setTotalHours(credits * 16); // 设置总学时，按学分数 * 16计算
        course.setTheoryHours(credits * 12); // 理论学时
        course.setPracticeHours(credits * 4); // 实践学时
        course.setDescription(description);
        course.setDepartment("计算机学院"); // 设置默认院系
        course.setStatus(CourseStatus.ACTIVE);
        course.setCreatedAt(LocalDateTime.now());
        return courseRepository.save(course);
    }
    
    private CourseSchedule createCourseSchedule(Course course, Teacher teacher, Semester semester, 
                                              String classroom, int dayOfWeek, LocalTime startTime, 
                                              LocalTime endTime, int maxStudents) {
        CourseSchedule schedule = new CourseSchedule();
        schedule.setCourse(course);
        schedule.setTeacher(teacher);
        schedule.setSemester(semester);
        schedule.setClassroom(classroom);
        schedule.setMaxStudents(maxStudents);
        schedule.setCurrentStudents(0);
        schedule.setStatus(ScheduleStatus.OPEN);
        
        // 设置选课时间（当前时间开始，一个月后结束）
        LocalDateTime now = LocalDateTime.now();
        schedule.setSelectionStartTime(now.minusDays(1)); // 昨天开始
        schedule.setSelectionEndTime(now.plusMonths(1)); // 一个月后结束
        
        // 创建时间段
        List<Map<String, Object>> scheduleTime = new ArrayList<>();
        Map<String, Object> timeSlot = new HashMap<>();
        timeSlot.put("dayOfWeek", dayOfWeek);
        timeSlot.put("startTime", startTime.toString());
        timeSlot.put("endTime", endTime.toString());
        scheduleTime.add(timeSlot);
        schedule.setScheduleTime(scheduleTime);
        
        schedule.setCreatedAt(LocalDateTime.now());
        return courseScheduleRepository.save(schedule);
    }
}