import { http } from '@/utils/request'
import type { 
  CourseSchedule, 
  CourseSelection, 
  CourseSelectionRequest,
  Course,
  CourseCreateRequest,
  CourseScheduleCreateRequest
} from '@/types/course'
import type { PageRequest, PageResponse } from '@/types/api'

export const courseApi = {
  // 学生选课相关
  getAvailableCourses(params: PageRequest): Promise<PageResponse<CourseSchedule>> {
    return http.get('/course-selection/available', { params })
  },
  
  selectCourse(data: CourseSelectionRequest): Promise<void> {
    return http.post('/course-selection/select', data)
  },
  
  dropCourse(courseScheduleId: number): Promise<void> {
    return http.post(`/course-selection/drop/${courseScheduleId}`)
  },
  
  getMySelectedCourses(params: PageRequest): Promise<PageResponse<CourseSelection>> {
    return http.get('/course-selection/my-courses', { params })
  },
  
  // 教师相关
  getStudentsByCourseSchedule(courseScheduleId: number, params: PageRequest): Promise<PageResponse<CourseSelection>> {
    return http.get(`/course-selection/course/${courseScheduleId}/students`, { params })
  },
  
  getMyCourses(semesterId?: number): Promise<CourseSchedule[]> {
    return http.get('/teacher/my-courses', { 
      params: semesterId ? { semesterId } : undefined 
    })
  },
  
  // 管理员课程管理
  getAllCourses(params: PageRequest): Promise<PageResponse<Course>> {
    return http.get('/admin/course-management/courses', { params })
  },
  
  createCourse(data: CourseCreateRequest): Promise<Course> {
    return http.post('/admin/course-management/courses', data)
  },
  
  deleteCourse(courseId: number): Promise<void> {
    return http.delete(`/admin/course-management/courses/${courseId}`)
  },
  
  // 课程安排管理
  getCourseSchedules(params: PageRequest & { semesterId?: number }): Promise<PageResponse<CourseSchedule>> {
    return http.get('/admin/course-management/course-schedules', { params })
  },
  
  createCourseSchedule(data: CourseScheduleCreateRequest): Promise<CourseSchedule> {
    return http.post('/admin/course-management/course-schedules', data)
  },
  
  updateCourseScheduleStatus(scheduleId: number, status: string): Promise<void> {
    return http.put(`/admin/course-management/course-schedules/${scheduleId}/status`, null, {
      params: { status }
    })
  },
  
  deleteCourseSchedule(scheduleId: number): Promise<void> {
    return http.delete(`/admin/course-management/course-schedules/${scheduleId}`)
  }
}