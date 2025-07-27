import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { UserRole } from '@/types/user'

// 路由组件懒加载
const Login = () => import('@/views/Login.vue')
const Register = () => import('@/views/Register.vue')
const Layout = () => import('@/layout/index.vue')
const Dashboard = () => import('@/views/Dashboard.vue')
const NotFound = () => import('@/views/404.vue')

// 学生路由
const StudentCourseSelection = () => import('@/views/student/CourseSelection.vue')
const StudentMyCourses = () => import('@/views/student/MyCourses.vue')
const StudentSchedule = () => import('@/views/student/Schedule.vue')

// 教师路由
const TeacherCourses = () => import('@/views/teacher/MyCourses.vue')
const TeacherStudents = () => import('@/views/teacher/Students.vue')

// 管理员路由
const AdminCourses = () => import('@/views/admin/Courses.vue')
const AdminCourseSchedules = () => import('@/views/admin/CourseSchedules.vue')
const AdminStudents = () => import('@/views/admin/Students.vue')
const AdminTeachers = () => import('@/views/admin/Teachers.vue')

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { 
      title: '登录',
      requireAuth: false,
      hideInMenu: true
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { 
      title: '注册',
      requireAuth: false,
      hideInMenu: true
    }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard,
        meta: { 
          title: '首页',
          icon: 'House',
          requireAuth: true
        }
      }
    ]
  },
  {
    path: '/student',
    component: Layout,
    meta: {
      title: '学生功能',
      icon: 'User',
      requireAuth: true,
      roles: [UserRole.STUDENT]
    },
    children: [
      {
        path: 'course-selection',
        name: 'StudentCourseSelection',
        component: StudentCourseSelection,
        meta: { 
          title: '选课中心',
          icon: 'EditPen',
          requireAuth: true,
          roles: [UserRole.STUDENT]
        }
      },
      {
        path: 'my-courses',
        name: 'StudentMyCourses',
        component: StudentMyCourses,
        meta: { 
          title: '我的课程',
          icon: 'Document',
          requireAuth: true,
          roles: [UserRole.STUDENT]
        }
      },
      {
        path: 'schedule',
        name: 'StudentSchedule',
        component: StudentSchedule,
        meta: { 
          title: '课程表',
          icon: 'Calendar',
          requireAuth: true,
          roles: [UserRole.STUDENT]
        }
      }
    ]
  },
  {
    path: '/teacher',
    component: Layout,
    meta: {
      title: '教师功能',
      icon: 'Avatar',
      requireAuth: true,
      roles: [UserRole.TEACHER]
    },
    children: [
      {
        path: 'my-courses',
        name: 'TeacherCourses',
        component: TeacherCourses,
        meta: { 
          title: '我的课程',
          icon: 'Document',
          requireAuth: true,
          roles: [UserRole.TEACHER]
        }
      },
      {
        path: 'students',
        name: 'TeacherStudents',
        component: TeacherStudents,
        meta: { 
          title: '学生管理',
          icon: 'UserFilled',
          requireAuth: true,
          roles: [UserRole.TEACHER]
        }
      }
    ]
  },
  {
    path: '/admin',
    component: Layout,
    meta: {
      title: '系统管理',
      icon: 'Setting',
      requireAuth: true,
      roles: [UserRole.ADMIN]
    },
    children: [
      {
        path: 'courses',
        name: 'AdminCourses',
        component: AdminCourses,
        meta: { 
          title: '课程管理',
          icon: 'Document',
          requireAuth: true,
          roles: [UserRole.ADMIN]
        }
      },
      {
        path: 'course-schedules',
        name: 'AdminCourseSchedules',
        component: AdminCourseSchedules,
        meta: { 
          title: '课程安排',
          icon: 'Calendar',
          requireAuth: true,
          roles: [UserRole.ADMIN]
        }
      },
      {
        path: 'students',
        name: 'AdminStudents',
        component: AdminStudents,
        meta: { 
          title: '学生管理',
          icon: 'User',
          requireAuth: true,
          roles: [UserRole.ADMIN]
        }
      },
      {
        path: 'teachers',
        name: 'AdminTeachers',
        component: AdminTeachers,
        meta: { 
          title: '教师管理',
          icon: 'Avatar',
          requireAuth: true,
          roles: [UserRole.ADMIN]
        }
      }
    ]
  },
  {
    path: '/404',
    name: 'NotFound',
    component: NotFound,
    meta: { 
      title: '页面不存在',
      requireAuth: false,
      hideInMenu: true
    }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 学生选课系统` : '学生选课系统'
  
  // 不需要认证的页面直接通过
  if (!to.meta.requireAuth) {
    // 如果已登录用户访问登录页，重定向到首页
    if ((to.name === 'Login' || to.name === 'Register') && userStore.isLoggedIn) {
      next('/')
    } else {
      next()
    }
    return
  }
  
  // 需要认证的页面检查登录状态
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    next('/login')
    return
  }
  
  // 检查角色权限
  if (to.meta.roles && to.meta.roles.length > 0) {
    const userRole = userStore.userInfo?.role
    if (!userRole || !to.meta.roles.includes(userRole)) {
      ElMessage.error('权限不足，无法访问该页面')
      next('/dashboard')
      return
    }
  }
  
  next()
})

export default router