package com.example.courseselection.service;

import com.example.courseselection.common.exception.BusinessException;
import com.example.courseselection.common.result.ResultCode;
import com.example.courseselection.dto.request.CourseCreateRequest;
import com.example.courseselection.dto.request.CourseScheduleCreateRequest;
import com.example.courseselection.dto.response.CourseScheduleDTO;
import com.example.courseselection.entity.*;
import com.example.courseselection.entity.enums.CourseStatus;
import com.example.courseselection.entity.enums.ScheduleStatus;
import com.example.courseselection.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseManagementService {

    private final CourseRepository courseRepository;
    private final CourseScheduleRepository courseScheduleRepository;
    private final TeacherRepository teacherRepository;
    private final SemesterRepository semesterRepository;

    @Transactional
    public Course createCourse(CourseCreateRequest request) {
        // 检查课程代码是否已存在
        if (courseRepository.existsByCourseCode(request.getCourseCode())) {
            throw new BusinessException(ResultCode.COURSE_ALREADY_EXISTS, "课程代码已存在");
        }

        // 验证理论学时+实践学时=总学时
        if (request.getTheoryHours() + request.getPracticeHours() != request.getTotalHours()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "理论学时与实践学时之和必须等于总学时");
        }

        Course course = new Course();
        course.setCourseCode(request.getCourseCode());
        course.setCourseName(request.getCourseName());
        course.setCourseType(request.getCourseType());
        course.setCredits(request.getCredits());
        course.setTotalHours(request.getTotalHours());
        course.setTheoryHours(request.getTheoryHours());
        course.setPracticeHours(request.getPracticeHours());
        course.setDescription(request.getDescription());
        course.setPrerequisiteCourses(request.getPrerequisiteCourses());
        course.setDepartment(request.getDepartment());
        course.setStatus(CourseStatus.ACTIVE);

        Course savedCourse = courseRepository.save(course);
        log.info("Created new course: {}", savedCourse.getCourseCode());
        return savedCourse;
    }

    @Transactional
    public CourseSchedule createCourseSchedule(CourseScheduleCreateRequest request) {
        // 验证课程存在
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new BusinessException(ResultCode.COURSE_NOT_FOUND));

        // 验证教师存在
        Teacher teacher = teacherRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND, "教师不存在"));

        // 验证学期存在
        Semester semester = semesterRepository.findById(request.getSemesterId())
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "学期不存在"));

        // 验证选课时间
        if (request.getSelectionStartTime() != null && request.getSelectionEndTime() != null) {
            if (request.getSelectionStartTime().isAfter(request.getSelectionEndTime())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "选课开始时间不能晚于结束时间");
            }
        }

        CourseSchedule schedule = new CourseSchedule();
        schedule.setCourse(course);
        schedule.setTeacher(teacher);
        schedule.setSemester(semester);
        schedule.setClassName(request.getClassName());
        schedule.setMaxStudents(request.getMaxStudents());
        schedule.setCurrentStudents(0);
        schedule.setClassroom(request.getClassroom());
        schedule.setScheduleTime(request.getScheduleTime());
        schedule.setSelectionStartTime(request.getSelectionStartTime());
        schedule.setSelectionEndTime(request.getSelectionEndTime());
        schedule.setStatus(ScheduleStatus.PENDING);

        CourseSchedule savedSchedule = courseScheduleRepository.save(schedule);
        log.info("Created course schedule for course {} by teacher {}", 
                course.getCourseCode(), teacher.getUser().getRealName());
        return savedSchedule;
    }

    @Transactional(readOnly = true)
    public Page<Course> getAllCourses(Pageable pageable) {
        return courseRepository.findByStatus(CourseStatus.ACTIVE, pageable);
    }

    @Transactional(readOnly = true)
    public Page<CourseScheduleDTO> getCourseSchedules(Long semesterId, Pageable pageable) {
        Page<CourseSchedule> schedules;
        if (semesterId != null) {
            schedules = courseScheduleRepository.findBySemesterIdAndStatus(semesterId, null, pageable);
        } else {
            schedules = courseScheduleRepository.findAll(pageable);
        }

        return schedules.map(this::convertToCourseScheduleDTO);
    }

    @Transactional(readOnly = true)
    public List<CourseSchedule> getTeacherCourseSchedules(Long teacherId, Long semesterId) {
        if (semesterId != null) {
            return courseScheduleRepository.findByTeacherIdAndSemesterId(teacherId, semesterId);
        } else {
            return courseScheduleRepository.findByTeacherId(teacherId);
        }
    }

    @Transactional
    public void updateCourseScheduleStatus(Long scheduleId, ScheduleStatus status) {
        CourseSchedule schedule = courseScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BusinessException(ResultCode.COURSE_NOT_FOUND, "课程安排不存在"));

        schedule.setStatus(status);
        courseScheduleRepository.save(schedule);

        log.info("Updated course schedule {} status to {}", scheduleId, status);
    }

    @Transactional
    public void deleteCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ResultCode.COURSE_NOT_FOUND));

        // 检查是否有相关的课程安排
        List<CourseSchedule> schedules = courseScheduleRepository.findByCourseId(courseId);
        if (!schedules.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该课程存在课程安排，无法删除");
        }

        course.setStatus(CourseStatus.INACTIVE);
        courseRepository.save(course);

        log.info("Deleted course: {}", course.getCourseCode());
    }

    @Transactional
    public void deleteCourseSchedule(Long scheduleId) {
        CourseSchedule schedule = courseScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BusinessException(ResultCode.COURSE_NOT_FOUND, "课程安排不存在"));

        // 检查是否有学生选课
        if (schedule.getCurrentStudents() > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该课程安排已有学生选课，无法删除");
        }

        courseScheduleRepository.delete(schedule);
        log.info("Deleted course schedule: {}", scheduleId);
    }

    private CourseScheduleDTO convertToCourseScheduleDTO(CourseSchedule schedule) {
        CourseScheduleDTO dto = new CourseScheduleDTO();
        dto.setId(schedule.getId());
        dto.setClassName(schedule.getClassName());
        dto.setMaxStudents(schedule.getMaxStudents());
        dto.setCurrentStudents(schedule.getCurrentStudents());
        dto.setClassroom(schedule.getClassroom());
        dto.setScheduleTime(schedule.getScheduleTime());
        dto.setSelectionStartTime(schedule.getSelectionStartTime());
        dto.setSelectionEndTime(schedule.getSelectionEndTime());
        dto.setStatus(schedule.getStatus());

        // 课程信息
        Course course = schedule.getCourse();
        dto.setCourseId(course.getId());
        dto.setCourseCode(course.getCourseCode());
        dto.setCourseName(course.getCourseName());
        dto.setCourseType(course.getCourseType());
        dto.setCredits(course.getCredits());
        dto.setTotalHours(course.getTotalHours());
        dto.setDescription(course.getDescription());
        dto.setDepartment(course.getDepartment());

        // 教师信息
        Teacher teacher = schedule.getTeacher();
        dto.setTeacherId(teacher.getId());
        dto.setTeacherName(teacher.getUser().getRealName());
        dto.setTeacherTitle(teacher.getTitle());
        dto.setTeacherDepartment(teacher.getDepartment());

        // 学期信息
        Semester semester = schedule.getSemester();
        dto.setSemesterId(semester.getId());
        dto.setSemesterName(semester.getName());

        return dto;
    }
}