export enum UserRole {
  STUDENT = 'STUDENT',
  TEACHER = 'TEACHER',
  ADMIN = 'ADMIN'
}

export enum UserStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  SUSPENDED = 'SUSPENDED'
}

export interface UserInfo {
  id: number
  username: string
  email?: string
  realName: string
  role: UserRole
  
  // 学生信息
  studentNumber?: string
  grade?: string
  major?: string
  className?: string
  
  // 教师信息
  teacherNumber?: string
  department?: string
  title?: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  userInfo: UserInfo
}

export interface RegisterRequest {
  username: string
  password: string
  email?: string
  phone?: string
  realName: string
  gender?: 'MALE' | 'FEMALE'
  role: UserRole
  
  // 学生字段
  studentNumber?: string
  grade?: string
  major?: string
  className?: string
  enrollmentYear?: number
  
  // 教师字段
  teacherNumber?: string
  department?: string
  title?: string
  researchDirection?: string
  officeLocation?: string
}