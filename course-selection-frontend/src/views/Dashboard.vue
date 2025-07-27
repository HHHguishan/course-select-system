<template>
  <div class="dashboard">
    <div class="page-header">
      <h1 class="page-title">欢迎来到学生选课系统</h1>
      <p class="page-description">{{ welcomeMessage }}</p>
    </div>

    <div class="dashboard-content">
      <!-- 用户信息卡片 -->
      <div class="card">
        <h3>个人信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名">
            {{ userStore.userInfo?.realName }}
          </el-descriptions-item>
          <el-descriptions-item label="用户名">
            {{ userStore.userInfo?.username }}
          </el-descriptions-item>
          <el-descriptions-item label="角色">
            <el-tag :type="getRoleTagType(userStore.userInfo?.role)">
              {{ getRoleText(userStore.userInfo?.role) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="邮箱">
            {{ userStore.userInfo?.email || '未设置' }}
          </el-descriptions-item>
          
          <!-- 学生信息 -->
          <template v-if="userStore.isStudent">
            <el-descriptions-item label="学号">
              {{ userStore.userInfo?.studentNumber }}
            </el-descriptions-item>
            <el-descriptions-item label="专业">
              {{ userStore.userInfo?.major }}
            </el-descriptions-item>
          </template>
          
          <!-- 教师信息 -->
          <template v-if="userStore.isTeacher">
            <el-descriptions-item label="工号">
              {{ userStore.userInfo?.teacherNumber }}
            </el-descriptions-item>
            <el-descriptions-item label="院系">
              {{ userStore.userInfo?.department }}
            </el-descriptions-item>
          </template>
        </el-descriptions>
      </div>

      <!-- 快捷功能 -->
      <div class="card">
        <h3>快捷功能</h3>
        <div class="quick-actions">
          <!-- 学生功能 -->
          <template v-if="userStore.isStudent">
            <el-button type="primary" @click="$router.push('/student/course-selection')">
              <el-icon><EditPen /></el-icon>
              选课中心
            </el-button>
            <el-button type="success" @click="$router.push('/student/my-courses')">
              <el-icon><Document /></el-icon>
              我的课程
            </el-button>
            <el-button type="info" @click="$router.push('/student/schedule')">
              <el-icon><Calendar /></el-icon>
              课程表
            </el-button>
          </template>
          
          <!-- 教师功能 -->
          <template v-if="userStore.isTeacher">
            <el-button type="primary" @click="$router.push('/teacher/my-courses')">
              <el-icon><Document /></el-icon>
              我的课程
            </el-button>
            <el-button type="success" @click="$router.push('/teacher/students')">
              <el-icon><UserFilled /></el-icon>
              学生管理
            </el-button>
          </template>
          
          <!-- 管理员功能 -->
          <template v-if="userStore.isAdmin">
            <el-button type="primary" @click="$router.push('/admin/courses')">
              <el-icon><Document /></el-icon>
              课程管理
            </el-button>
            <el-button type="success" @click="$router.push('/admin/course-schedules')">
              <el-icon><Calendar /></el-icon>
              课程安排
            </el-button>
            <el-button type="info" @click="$router.push('/admin/students')">
              <el-icon><User /></el-icon>
              学生管理
            </el-button>
            <el-button type="warning" @click="$router.push('/admin/teachers')">
              <el-icon><Avatar /></el-icon>
              教师管理
            </el-button>
          </template>
        </div>
      </div>

      <!-- 系统公告 -->
      <div class="card">
        <h3>系统公告</h3>
        <el-alert
          title="欢迎使用学生选课系统"
          type="info"
          :closable="false"
          show-icon
        >
          <p>这是一个基于Vue 3 + Spring Boot开发的现代化选课系统</p>
          <p>支持学生选课、教师管理、管理员配置等功能</p>
        </el-alert>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { UserRole } from '@/types/user'

const userStore = useUserStore()

const welcomeMessage = computed(() => {
  const role = userStore.userInfo?.role
  const name = userStore.userInfo?.realName
  
  switch (role) {
    case UserRole.STUDENT:
      return `欢迎您，${name}同学！您可以在这里进行选课、查看课程表等操作。`
    case UserRole.TEACHER:
      return `欢迎您，${name}老师！您可以管理您的课程和查看学生信息。`
    case UserRole.ADMIN:
      return `欢迎您，${name}管理员！您可以管理系统的各项配置。`
    default:
      return `欢迎使用学生选课系统！`
  }
})

const getRoleText = (role?: UserRole) => {
  switch (role) {
    case UserRole.STUDENT:
      return '学生'
    case UserRole.TEACHER:
      return '教师'
    case UserRole.ADMIN:
      return '管理员'
    default:
      return '未知'
  }
}

const getRoleTagType = (role?: UserRole) => {
  switch (role) {
    case UserRole.STUDENT:
      return 'primary'
    case UserRole.TEACHER:
      return 'success'
    case UserRole.ADMIN:
      return 'danger'
    default:
      return 'info'
  }
}
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.dashboard-content {
  display: grid;
  gap: 20px;
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
}

.card h3 {
  margin-bottom: 16px;
  color: #303133;
  font-weight: 600;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.quick-actions .el-button {
  display: flex;
  align-items: center;
  gap: 8px;
}

@media (max-width: 768px) {
  .dashboard-content {
    grid-template-columns: 1fr;
  }
  
  .quick-actions {
    flex-direction: column;
  }
  
  .quick-actions .el-button {
    justify-content: center;
  }
}
</style>