-- 学生选课系统数据库建表语句
-- 数据库名称: course_selection_system

CREATE DATABASE IF NOT EXISTS course_selection_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE course_selection_system;

-- 1. 用户表 (统一管理学生、教师、管理员)
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(加密)',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    gender ENUM('MALE', 'FEMALE') COMMENT '性别',
    role ENUM('STUDENT', 'TEACHER', 'ADMIN') NOT NULL COMMENT '角色',
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE' COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_role (role),
    INDEX idx_status (status)
) COMMENT='用户表';

-- 2. 学生表 (扩展学生信息)
CREATE TABLE students (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE COMMENT '关联用户表ID',
    student_number VARCHAR(20) NOT NULL UNIQUE COMMENT '学号',
    grade VARCHAR(10) COMMENT '年级',
    major VARCHAR(100) COMMENT '专业',
    class_name VARCHAR(50) COMMENT '班级',
    enrollment_year YEAR COMMENT '入学年份',
    max_credits INT DEFAULT 30 COMMENT '最大可选学分',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_student_number (student_number),
    INDEX idx_grade (grade),
    INDEX idx_major (major)
) COMMENT='学生表';

-- 3. 教师表 (扩展教师信息)
CREATE TABLE teachers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE COMMENT '关联用户表ID',
    teacher_number VARCHAR(20) NOT NULL UNIQUE COMMENT '工号',
    department VARCHAR(100) COMMENT '院系',
    title VARCHAR(50) COMMENT '职称',
    research_direction TEXT COMMENT '研究方向',
    office_location VARCHAR(100) COMMENT '办公地点',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_teacher_number (teacher_number),
    INDEX idx_department (department)
) COMMENT='教师表';

-- 4. 学期表
CREATE TABLE semesters (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '学期名称',
    academic_year VARCHAR(20) NOT NULL COMMENT '学年',
    semester_type ENUM('SPRING', 'AUTUMN', 'SUMMER') NOT NULL COMMENT '学期类型',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE NOT NULL COMMENT '结束日期',
    is_current BOOLEAN DEFAULT FALSE COMMENT '是否当前学期',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_academic_year (academic_year),
    INDEX idx_is_current (is_current)
) COMMENT='学期表';

-- 5. 课程表
CREATE TABLE courses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_code VARCHAR(20) NOT NULL UNIQUE COMMENT '课程代码',
    course_name VARCHAR(100) NOT NULL COMMENT '课程名称',
    course_type ENUM('REQUIRED', 'ELECTIVE', 'PUBLIC') NOT NULL COMMENT '课程类型',
    credits DECIMAL(3,1) NOT NULL COMMENT '学分',
    total_hours INT NOT NULL COMMENT '总学时',
    theory_hours INT DEFAULT 0 COMMENT '理论学时',
    practice_hours INT DEFAULT 0 COMMENT '实践学时',
    description TEXT COMMENT '课程描述',
    prerequisite_courses JSON COMMENT '前置课程ID列表',
    department VARCHAR(100) COMMENT '开课院系',
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE' COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_course_code (course_code),
    INDEX idx_course_type (course_type),
    INDEX idx_department (department),
    INDEX idx_status (status)
) COMMENT='课程表';

-- 6. 课程安排表 (具体某学期的课程安排)
CREATE TABLE course_schedules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL COMMENT '课程ID',
    teacher_id BIGINT NOT NULL COMMENT '授课教师ID',
    semester_id BIGINT NOT NULL COMMENT '学期ID',
    class_name VARCHAR(50) COMMENT '班级名称',
    max_students INT NOT NULL COMMENT '最大选课人数',
    current_students INT DEFAULT 0 COMMENT '当前选课人数',
    classroom VARCHAR(50) COMMENT '教室',
    schedule_time JSON COMMENT '上课时间安排',
    selection_start_time DATETIME COMMENT '选课开始时间',
    selection_end_time DATETIME COMMENT '选课结束时间',
    status ENUM('PENDING', 'OPEN', 'CLOSED', 'CANCELLED') DEFAULT 'PENDING' COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE CASCADE,
    FOREIGN KEY (semester_id) REFERENCES semesters(id) ON DELETE CASCADE,
    INDEX idx_course_teacher_semester (course_id, teacher_id, semester_id),
    INDEX idx_selection_time (selection_start_time, selection_end_time),
    INDEX idx_status (status)
) COMMENT='课程安排表';

-- 7. 选课记录表
CREATE TABLE course_selections (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL COMMENT '学生ID',
    course_schedule_id BIGINT NOT NULL COMMENT '课程安排ID',
    selection_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
    status ENUM('SELECTED', 'DROPPED', 'COMPLETED') DEFAULT 'SELECTED' COMMENT '选课状态',
    grade DECIMAL(5,2) COMMENT '成绩',
    grade_point DECIMAL(3,2) COMMENT '绩点',
    is_passed BOOLEAN COMMENT '是否通过',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (course_schedule_id) REFERENCES course_schedules(id) ON DELETE CASCADE,
    UNIQUE KEY uk_student_course_schedule (student_id, course_schedule_id),
    INDEX idx_student_status (student_id, status),
    INDEX idx_course_schedule (course_schedule_id),
    INDEX idx_selection_time (selection_time)
) COMMENT='选课记录表';

-- 8. 成绩表 (可选，如果需要更详细的成绩管理)
CREATE TABLE grades (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_selection_id BIGINT NOT NULL COMMENT '选课记录ID',
    regular_score DECIMAL(5,2) COMMENT '平时成绩',
    midterm_score DECIMAL(5,2) COMMENT '期中成绩',
    final_score DECIMAL(5,2) COMMENT '期末成绩',
    total_score DECIMAL(5,2) COMMENT '总成绩',
    grade_point DECIMAL(3,2) COMMENT '绩点',
    grade_level VARCHAR(10) COMMENT '等级成绩',
    is_makeup BOOLEAN DEFAULT FALSE COMMENT '是否补考',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (course_selection_id) REFERENCES course_selections(id) ON DELETE CASCADE,
    INDEX idx_course_selection (course_selection_id)
) COMMENT='成绩表';

-- 9. 系统日志表
CREATE TABLE system_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT '操作用户ID',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型',
    operation_detail TEXT COMMENT '操作详情',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent TEXT COMMENT '用户代理',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_user_operation (user_id, operation_type),
    INDEX idx_created_at (created_at)
) COMMENT='系统日志表';

-- 10. 系统配置表
CREATE TABLE system_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    description TEXT COMMENT '配置描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_config_key (config_key)
) COMMENT='系统配置表';

-- 插入默认数据
-- 默认管理员用户
INSERT INTO users (username, password, real_name, role) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFVMLkYi6BY.15UYnAY3/D6', '系统管理员', 'ADMIN');

-- 默认学期
INSERT INTO semesters (name, academic_year, semester_type, start_date, end_date, is_current) 
VALUES ('2024-2025学年春季学期', '2024-2025', 'SPRING', '2025-02-01', '2025-07-31', TRUE);

-- 系统配置
INSERT INTO system_config (config_key, config_value, description) VALUES
('max_selection_credits', '30', '学生最大选课学分'),
('selection_start_days_before', '7', '选课开始前几天可以查看课程'),
('allow_drop_days', '14', '开课后几天内允许退课');