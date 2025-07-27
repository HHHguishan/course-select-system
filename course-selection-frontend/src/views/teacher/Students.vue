<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">学生管理</h1>
      <p class="page-description">查看所有选课学生信息</p>
    </div>

    <div class="card">
      <div class="table-header">
        <div class="filter-section">
          <el-select 
            v-model="selectedCourse" 
            placeholder="选择课程" 
            style="width: 300px; margin-right: 16px;"
            clearable
            @change="loadStudents"
          >
            <el-option
              v-for="course in courseList"
              :key="course.id"
              :label="`${course.course.courseCode} - ${course.course.courseName}`"
              :value="course.id"
            />
          </el-select>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索学号或姓名"
            style="width: 250px; margin-right: 16px;"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" @click="searchStudents">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
        </div>
        <el-button @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>

      <!-- 统计信息 -->
      <div class="stats-row" v-if="selectedCourse">
        <el-card class="stats-card">
          <el-statistic title="总选课人数" :value="pagination.total">
            <template #suffix>
              <el-icon><User /></el-icon>
            </template>
          </el-statistic>
        </el-card>
        <el-card class="stats-card">
          <el-statistic title="已确认" :value="confirmedCount">
            <template #suffix>
              <el-icon style="color: #67c23a"><SuccessFilled /></el-icon>
            </template>
          </el-statistic>
        </el-card>
        <el-card class="stats-card">
          <el-statistic title="待确认" :value="pendingCount">
            <template #suffix>
              <el-icon style="color: #e6a23c"><Warning /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </div>

      <el-table
        v-loading="loading"
        :data="studentsList"
        style="width: 100%"
        border
      >
        <el-table-column prop="student.studentNumber" label="学号" width="120" />
        <el-table-column prop="student.realName" label="姓名" width="100" />
        <el-table-column prop="student.major" label="专业" min-width="150" />
        <el-table-column prop="student.email" label="邮箱" min-width="200" />
        <el-table-column label="课程信息" min-width="200" v-if="!selectedCourse">
          <template #default="scope">
            <div>
              <div class="font-medium">{{ scope.row.courseSchedule.course.courseName }}</div>
              <div class="text-sm text-gray-500">{{ scope.row.courseSchedule.course.courseCode }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="选课时间" width="150">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="viewStudentDetail(scope.row.student)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-3">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 学生详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="学生详情"
      width="600px"
    >
      <el-descriptions v-if="selectedStudent" :column="2" border>
        <el-descriptions-item label="学号">
          {{ selectedStudent.studentNumber }}
        </el-descriptions-item>
        <el-descriptions-item label="姓名">
          {{ selectedStudent.realName }}
        </el-descriptions-item>
        <el-descriptions-item label="用户名">
          {{ selectedStudent.username }}
        </el-descriptions-item>
        <el-descriptions-item label="邮箱">
          {{ selectedStudent.email }}
        </el-descriptions-item>
        <el-descriptions-item label="专业" span="2">
          {{ selectedStudent.major }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { courseApi } from '@/api/course'
import { ElMessage } from 'element-plus'
import type { CourseSchedule, CourseSelection } from '@/types/course'
import type { UserInfo } from '@/types/user'
import dayjs from 'dayjs'

const loading = ref(false)
const searchKeyword = ref('')
const selectedCourse = ref<number | null>(null)
const detailDialogVisible = ref(false)
const selectedStudent = ref<UserInfo | null>(null)

const courseList = ref<CourseSchedule[]>([])
const studentsList = ref<CourseSelection[]>([])

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 统计数据
const confirmedCount = computed(() => {
  return studentsList.value.filter(s => s.status === 'CONFIRMED').length
})

const pendingCount = computed(() => {
  return studentsList.value.filter(s => s.status === 'PENDING').length
})

// 加载我的课程列表（用于筛选）
const loadCourses = async () => {
  try {
    const courses = await courseApi.getMyCourses()
    courseList.value = courses
  } catch (error) {
    console.error('加载课程列表失败:', error)
  }
}

// 加载学生列表
const loadStudents = async () => {
  if (!selectedCourse.value) {
    studentsList.value = []
    pagination.total = 0
    return
  }

  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      keyword: searchKeyword.value || undefined
    }
    const response = await courseApi.getStudentsByCourseSchedule(selectedCourse.value, params)
    studentsList.value = response.content
    pagination.total = response.totalElements
  } catch (error) {
    console.error('加载学生列表失败:', error)
    ElMessage.error('加载学生列表失败')
  } finally {
    loading.value = false
  }
}

const searchStudents = () => {
  pagination.page = 1
  loadStudents()
}

const refreshData = () => {
  searchKeyword.value = ''
  pagination.page = 1
  loadCourses()
  loadStudents()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  loadStudents()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  loadStudents()
}

// 查看学生详情
const viewStudentDetail = (student: UserInfo) => {
  selectedStudent.value = student
  detailDialogVisible.value = true
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm')
}

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  switch (status) {
    case 'CONFIRMED':
      return 'success'
    case 'PENDING':
      return 'warning'
    case 'CANCELLED':
      return 'danger'
    default:
      return 'info'
  }
}

// 获取状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'CONFIRMED':
      return '已确认'
    case 'PENDING':
      return '待确认'
    case 'CANCELLED':
      return '已取消'
    default:
      return '未知'
  }
}

onMounted(() => {
  loadCourses()
})
</script>

<style scoped>
.filter-section {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.stats-card {
  text-align: center;
}

.font-medium {
  font-weight: 500;
}

.text-sm {
  font-size: 0.875rem;
}

.text-gray-500 {
  color: #6b7280;
}

@media (max-width: 768px) {
  .filter-section {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-section .el-select,
  .filter-section .el-input {
    width: 100% !important;
    margin-right: 0 !important;
    margin-bottom: 8px;
  }
}
</style>