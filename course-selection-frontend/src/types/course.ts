export enum CourseType {
  REQUIRED = 'REQUIRED',
  ELECTIVE = 'ELECTIVE',
  PUBLIC = 'PUBLIC'
}

export enum CourseStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE'
}

export enum ScheduleStatus {
  PENDING = 'PENDING',
  OPEN = 'OPEN',
  CLOSED = 'CLOSED',
  CANCELLED = 'CANCELLED'
}

export enum SelectionStatus {
  SELECTED = 'SELECTED',
  DROPPED = 'DROPPED',
  COMPLETED = 'COMPLETED'
}

export interface Course {
  id: number
  courseCode: string
  courseName: string
  courseType: CourseType
  credits: number
  totalHours: number
  theoryHours: number
  practiceHours: number
  description?: string
  prerequisiteCourses?: number[]
  department?: string
  status: CourseStatus
}

export interface CourseSchedule {
  id: number
  className?: string
  maxStudents: number
  currentStudents: number
  classroom?: string
  scheduleTime?: any[]
  selectionStartTime?: string
  selectionEndTime?: string
  status: ScheduleStatus
  
  // 课程信息
  courseId: number
  courseCode: string
  courseName: string
  courseType: CourseType
  credits: number
  totalHours: number
  description?: string
  department?: string
  
  // 教师信息
  teacherId: number
  teacherName: string
  teacherTitle?: string
  teacherDepartment?: string
  
  // 学期信息
  semesterId: number
  semesterName: string
  
  // 选课状态（学生视角）
  isSelected?: boolean
  canSelect?: boolean
  selectionMessage?: string
}

export interface CourseSelection {
  id: number
  selectionTime: string
  status: SelectionStatus
  grade?: number
  gradePoint?: number
  isPassed?: boolean
  
  // 课程安排信息
  courseScheduleId: number
  className?: string
  classroom?: string
  scheduleTime?: any[]
  
  // 课程信息
  courseId: number
  courseCode: string
  courseName: string
  courseType: CourseType
  credits: number
  totalHours: number
  description?: string
  
  // 教师信息
  teacherId: number
  teacherName: string
  teacherTitle?: string
  
  // 学期信息
  semesterId: number
  semesterName: string
  
  // 学生信息（教师视角）
  studentId?: number
  studentNumber?: string
  studentName?: string
  studentMajor?: string
  studentClass?: string
}

export interface Semester {
  id: number
  name: string
  academicYear: string
  semesterType: 'SPRING' | 'AUTUMN' | 'SUMMER'
  startDate: string
  endDate: string
  isCurrent: boolean
}

export interface CourseCreateRequest {
  courseCode: string
  courseName: string
  courseType: CourseType
  credits: number
  totalHours: number
  theoryHours: number
  practiceHours: number
  description?: string
  prerequisiteCourses?: number[]
  department?: string
}

export interface CourseScheduleCreateRequest {
  courseId: number
  teacherId: number
  semesterId: number
  className?: string
  maxStudents: number
  classroom?: string
  scheduleTime?: any[]
  selectionStartTime?: string
  selectionEndTime?: string
}

export interface CourseSelectionRequest {
  courseScheduleId: number
}