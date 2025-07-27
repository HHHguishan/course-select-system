package com.example.courseselection.service;

import com.example.courseselection.common.exception.BusinessException;
import com.example.courseselection.common.result.ResultCode;
import com.example.courseselection.dto.response.CourseScheduleDTO;
import com.example.courseselection.dto.response.CourseSelectionDTO;
import com.example.courseselection.entity.*;
import com.example.courseselection.entity.enums.ScheduleStatus;
import com.example.courseselection.entity.enums.SelectionStatus;
import com.example.courseselection.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseSelectionService {

    private final CourseScheduleRepository courseScheduleRepository;
    private final CourseSelectionRepository courseSelectionRepository;
    private final StudentRepository studentRepository;
    private final SemesterRepository semesterRepository;
    private final CourseRepository courseRepository;

    @Value("${app.course-selection.default-max-credits:30}")
    private Integer defaultMaxCredits;

    @Value("${app.course-selection.default-drop-days:14}")
    private Integer defaultDropDays;

    @Transactional(readOnly = true)
    public Page<CourseScheduleDTO> getAvailableCourses(Long studentId, Pageable pageable) {
        // 获取当前学期
        Semester currentSemester = semesterRepository.findCurrentSemester()
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "当前学期未设置"));

        // 获取可选课程
        LocalDateTime now = LocalDateTime.now();
        List<CourseSchedule> availableSchedules = courseScheduleRepository
                .findAvailableCoursesForSelection(currentSemester.getId(), now);

        // 获取学生已选课程
        List<CourseSelection> selectedCourses = courseSelectionRepository
                .findByStudentIdAndSemesterIdAndStatus(studentId, currentSemester.getId(), SelectionStatus.SELECTED);

        // 转换为DTO并设置选课状态
        List<CourseScheduleDTO> courseDTOs = availableSchedules.stream()
                .map(schedule -> {
                    CourseScheduleDTO dto = convertToCourseScheduleDTO(schedule);
                    
                    // 检查是否已选
                    boolean isSelected = selectedCourses.stream()
                            .anyMatch(selection -> selection.getCourseSchedule().getId().equals(schedule.getId()));
                    dto.setIsSelected(isSelected);
                    
                    // 检查是否可以选课
                    validateCourseSelection(studentId, schedule, selectedCourses, dto);
                    
                    return dto;
                })
                .collect(Collectors.toList());

        // 分页处理
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), courseDTOs.size());
        List<CourseScheduleDTO> pageContent = courseDTOs.subList(start, end);

        return new PageImpl<>(pageContent, pageable, courseDTOs.size());
    }

    @Transactional
    public void selectCourse(Long studentId, Long courseScheduleId) {
        // 获取学生信息
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND, "学生不存在"));

        // 获取课程安排
        CourseSchedule courseSchedule = courseScheduleRepository.findById(courseScheduleId)
                .orElseThrow(() -> new BusinessException(ResultCode.COURSE_NOT_FOUND, "课程安排不存在"));

        // 验证选课条件
        validateCourseSelectionForAction(student, courseSchedule);

        // 检查是否已选
        if (courseSelectionRepository.existsByStudentIdAndCourseScheduleIdAndStatus(
                studentId, courseScheduleId, SelectionStatus.SELECTED)) {
            throw new BusinessException(ResultCode.ALREADY_SELECTED);
        }

        // 检查课程人数限制
        Long currentCount = courseSelectionRepository.countSelectedStudentsByCourseSchedule(courseScheduleId);
        if (currentCount >= courseSchedule.getMaxStudents()) {
            throw new BusinessException(ResultCode.COURSE_FULL);
        }

        // 创建选课记录
        CourseSelection courseSelection = new CourseSelection();
        courseSelection.setStudent(student);
        courseSelection.setCourseSchedule(courseSchedule);
        courseSelection.setSelectionTime(LocalDateTime.now());
        courseSelection.setStatus(SelectionStatus.SELECTED);

        courseSelectionRepository.save(courseSelection);

        // 更新课程当前人数
        courseSchedule.setCurrentStudents(currentCount.intValue() + 1);
        courseScheduleRepository.save(courseSchedule);

        log.info("Student {} selected course schedule {}", studentId, courseScheduleId);
    }

    @Transactional
    public void dropCourse(Long studentId, Long courseScheduleId) {
        // 获取选课记录
        CourseSelection courseSelection = courseSelectionRepository
                .findByStudentIdAndCourseScheduleId(studentId, courseScheduleId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_SELECTED, "未选择该课程"));

        if (courseSelection.getStatus() != SelectionStatus.SELECTED) {
            throw new BusinessException(ResultCode.NOT_SELECTED, "课程状态不允许退课");
        }

        // 检查退课时间限制
        CourseSchedule courseSchedule = courseSelection.getCourseSchedule();
        LocalDateTime now = LocalDateTime.now();
        
        // 如果选课时间已结束超过指定天数，不允许退课
        if (courseSchedule.getSelectionEndTime() != null && 
            now.isAfter(courseSchedule.getSelectionEndTime().plusDays(defaultDropDays))) {
            throw new BusinessException(ResultCode.DROP_TIME_EXPIRED);
        }

        // 更新选课状态
        courseSelection.setStatus(SelectionStatus.DROPPED);
        courseSelectionRepository.save(courseSelection);

        // 更新课程当前人数
        if (courseSchedule.getCurrentStudents() > 0) {
            courseSchedule.setCurrentStudents(courseSchedule.getCurrentStudents() - 1);
            courseScheduleRepository.save(courseSchedule);
        }

        log.info("Student {} dropped course schedule {}", studentId, courseScheduleId);
    }

    @Transactional(readOnly = true)
    public Page<CourseSelectionDTO> getMySelectedCourses(Long studentId, Pageable pageable) {
        Page<CourseSelection> selections = courseSelectionRepository
                .findByStudentIdAndStatus(studentId, SelectionStatus.SELECTED, pageable);

        return selections.map(this::convertToCourseSelectionDTO);
    }

    @Transactional(readOnly = true)
    public Page<CourseSelectionDTO> getStudentsByCourseSchedule(Long teacherId, Long courseScheduleId, Pageable pageable) {
        // 验证教师权限
        CourseSchedule courseSchedule = courseScheduleRepository.findById(courseScheduleId)
                .orElseThrow(() -> new BusinessException(ResultCode.COURSE_NOT_FOUND, "课程安排不存在"));

        if (!courseSchedule.getTeacher().getId().equals(teacherId)) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED, "无权查看该课程的学生名单");
        }

        List<CourseSelection> selections = courseSelectionRepository.findByCourseScheduleId(courseScheduleId);
        List<CourseSelectionDTO> dtos = selections.stream()
                .map(this::convertToCourseSelectionDTO)
                .collect(Collectors.toList());

        // 分页处理
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), dtos.size());
        List<CourseSelectionDTO> pageContent = dtos.subList(start, end);

        return new PageImpl<>(pageContent, pageable, dtos.size());
    }

    private void validateCourseSelection(Long studentId, CourseSchedule schedule, 
                                       List<CourseSelection> selectedCourses, CourseScheduleDTO dto) {
        dto.setCanSelect(true);
        dto.setSelectionMessage("");

        // 检查选课时间
        LocalDateTime now = LocalDateTime.now();
        if (schedule.getSelectionStartTime() != null && now.isBefore(schedule.getSelectionStartTime())) {
            dto.setCanSelect(false);
            dto.setSelectionMessage("选课尚未开始");
            return;
        }

        if (schedule.getSelectionEndTime() != null && now.isAfter(schedule.getSelectionEndTime())) {
            dto.setCanSelect(false);
            dto.setSelectionMessage("选课已结束");
            return;
        }

        // 检查课程状态
        if (schedule.getStatus() != ScheduleStatus.OPEN) {
            dto.setCanSelect(false);
            dto.setSelectionMessage("课程未开放选课");
            return;
        }

        // 检查人数限制
        if (schedule.getCurrentStudents() >= schedule.getMaxStudents()) {
            dto.setCanSelect(false);
            dto.setSelectionMessage("课程人数已满");
            return;
        }

        // 检查学分限制
        BigDecimal currentCredits = selectedCourses.stream()
                .map(selection -> selection.getCourseSchedule().getCourse().getCredits())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal newTotalCredits = currentCredits.add(schedule.getCourse().getCredits());
        if (newTotalCredits.compareTo(BigDecimal.valueOf(defaultMaxCredits)) > 0) {
            dto.setCanSelect(false);
            dto.setSelectionMessage("超出最大学分限制");
            return;
        }

        // 检查前置课程
        if (schedule.getCourse().getPrerequisiteCourses() != null && 
            !schedule.getCourse().getPrerequisiteCourses().isEmpty()) {
            // 这里可以添加前置课程检查逻辑
            // 暂时简化处理
        }
    }

    private void validateCourseSelectionForAction(Student student, CourseSchedule courseSchedule) {
        LocalDateTime now = LocalDateTime.now();

        // 检查选课时间
        if (courseSchedule.getSelectionStartTime() != null && now.isBefore(courseSchedule.getSelectionStartTime())) {
            throw new BusinessException(ResultCode.COURSE_NOT_OPEN, "选课尚未开始");
        }

        if (courseSchedule.getSelectionEndTime() != null && now.isAfter(courseSchedule.getSelectionEndTime())) {
            throw new BusinessException(ResultCode.COURSE_SELECTION_CLOSED);
        }

        // 检查课程状态
        if (courseSchedule.getStatus() != ScheduleStatus.OPEN) {
            throw new BusinessException(ResultCode.COURSE_NOT_OPEN);
        }

        // 检查学分限制
        Semester currentSemester = semesterRepository.findCurrentSemester()
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "当前学期未设置"));

        BigDecimal currentCredits = courseSelectionRepository
                .getTotalCreditsByStudentAndSemester(student.getId(), currentSemester.getId());
        
        if (currentCredits == null) {
            currentCredits = BigDecimal.ZERO;
        }

        BigDecimal newTotalCredits = currentCredits.add(courseSchedule.getCourse().getCredits());
        Integer maxCredits = student.getMaxCredits() != null ? student.getMaxCredits() : defaultMaxCredits;

        if (newTotalCredits.compareTo(BigDecimal.valueOf(maxCredits)) > 0) {
            throw new BusinessException(ResultCode.CREDITS_EXCEEDED);
        }
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

    private CourseSelectionDTO convertToCourseSelectionDTO(CourseSelection selection) {
        CourseSelectionDTO dto = new CourseSelectionDTO();
        dto.setId(selection.getId());
        dto.setSelectionTime(selection.getSelectionTime());
        dto.setStatus(selection.getStatus());
        dto.setGrade(selection.getGrade());
        dto.setGradePoint(selection.getGradePoint());
        dto.setIsPassed(selection.getIsPassed());

        // 课程安排信息
        CourseSchedule schedule = selection.getCourseSchedule();
        dto.setCourseScheduleId(schedule.getId());
        dto.setClassName(schedule.getClassName());
        dto.setClassroom(schedule.getClassroom());
        dto.setScheduleTime(schedule.getScheduleTime());

        // 课程信息
        Course course = schedule.getCourse();
        dto.setCourseId(course.getId());
        dto.setCourseCode(course.getCourseCode());
        dto.setCourseName(course.getCourseName());
        dto.setCourseType(course.getCourseType());
        dto.setCredits(course.getCredits());
        dto.setTotalHours(course.getTotalHours());
        dto.setDescription(course.getDescription());

        // 教师信息
        Teacher teacher = schedule.getTeacher();
        dto.setTeacherId(teacher.getId());
        dto.setTeacherName(teacher.getUser().getRealName());
        dto.setTeacherTitle(teacher.getTitle());

        // 学期信息
        Semester semester = schedule.getSemester();
        dto.setSemesterId(semester.getId());
        dto.setSemesterName(semester.getName());

        // 学生信息
        Student student = selection.getStudent();
        dto.setStudentId(student.getId());
        dto.setStudentNumber(student.getStudentNumber());
        dto.setStudentName(student.getUser().getRealName());
        dto.setStudentMajor(student.getMajor());
        dto.setStudentClass(student.getClassName());

        return dto;
    }
}