<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">我的课程</h1>
      <p class="page-description">管理您的授课课程</p>
    </div>

    <div class="card">
      <div class="table-header">
        <div class="semester-selector">
          <el-select v-model="selectedSemester" placeholder="选择学期" @change="loadCourses">
            <el-option label="当前学期" value="current" />
            <el-option label="2024春季学期" value="2024-spring" />
            <el-option label="2023秋季学期" value="2023-fall" />
          </el-select>
        </div>
        <el-button @click="loadCourses">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="courseList"
        style="width: 100%"
        border
      >
        <el-table-column label="课程代码" width="120">
          <template #default="scope">
            {{ scope.row.course.courseCode }}
          </template>
        </el-table-column>
        <el-table-column label="课程名称" min-width="200">
          <template #default="scope">
            {{ scope.row.course.courseName }}
          </template>
        </el-table-column>
        <el-table-column label="学分" width="80">
          <template #default="scope">
            {{ scope.row.course.credits }}
          </template>
        </el-table-column>
        <el-table-column prop="classroom" label="教室" width="120" />
        <el-table-column label="上课时间" width="150">
          <template #default="scope">
            {{ formatClassTime(scope.row) }}
          </template>
        </el-table-column>
        <el-table-column label="选课人数" width="100">
          <template #default="scope">
            <span>{{ scope.row.currentStudentCount }}/{{ scope.row.maxStudents }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="viewStudents(scope.row)"
            >
              查看学生
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 学生列表对话框 -->
    <el-dialog
      v-model="studentsDialogVisible"
      :title="`${selectedCourse?.course.courseName} - 学生列表`"
      width="800px"
    >
      <el-table
        v-loading="studentsLoading"
        :data="studentsList"
        style="width: 100%"
        border
      >
        <el-table-column prop="student.studentNumber" label="学号" width="120" />
        <el-table-column prop="student.realName" label="姓名" width="100" />
        <el-table-column prop="student.major" label="专业" min-width="150" />
        <el-table-column prop="student.email" label="邮箱" min-width="200" />
        <el-table-column label="选课时间" width="150">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getSelectionStatusTagType(scope.row.status)">
              {{ getSelectionStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-3">
        <el-pagination
          v-model:current-page="studentsPagination.page"
          v-model:page-size="studentsPagination.size"
          :total="studentsPagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleStudentsSizeChange"
          @current-change="handleStudentsCurrentChange"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { courseApi } from '@/api/course'
import { ElMessage } from 'element-plus'
import type { CourseSchedule, CourseSelection } from '@/types/course'
import dayjs from 'dayjs'

const loading = ref(false)
const studentsLoading = ref(false)
const selectedSemester = ref('current')
const studentsDialogVisible = ref(false)
const selectedCourse = ref<CourseSchedule | null>(null)

const courseList = ref<CourseSchedule[]>([])
const studentsList = ref<CourseSelection[]>([])

const studentsPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 加载我的课程
const loadCourses = async () => {
  loading.value = true
  try {
    // 这里可以根据selectedSemester传递学期ID
    const courses = await courseApi.getMyCourses()
    courseList.value = courses
  } catch (error) {
    console.error('加载课程失败:', error)
    ElMessage.error('加载课程数据失败')
  } finally {
    loading.value = false
  }
}

// 查看学生列表
const viewStudents = async (course: CourseSchedule) => {
  selectedCourse.value = course
  studentsDialogVisible.value = true
  await loadStudents()
}

// 加载学生列表
const loadStudents = async () => {
  if (!selectedCourse.value) return
  
  studentsLoading.value = true
  try {
    const params = {
      page: studentsPagination.page,
      size: studentsPagination.size
    }
    const response = await courseApi.getStudentsByCourseSchedule(selectedCourse.value.id, params)
    studentsList.value = response.content
    studentsPagination.total = response.totalElements
  } catch (error) {
    console.error('加载学生列表失败:', error)
    ElMessage.error('加载学生列表失败')
  } finally {
    studentsLoading.value = false
  }
}

const handleStudentsSizeChange = (size: number) => {
  studentsPagination.size = size
  studentsPagination.page = 1
  loadStudents()
}

const handleStudentsCurrentChange = (page: number) => {
  studentsPagination.page = page
  loadStudents()
}

// 格式化上课时间
const formatClassTime = (schedule: CourseSchedule) => {
  if (!schedule.timeSlot) return '待安排'
  
  const dayMap: Record<number, string> = {
    1: '周一', 2: '周二', 3: '周三', 4: '周四', 
    5: '周五', 6: '周六', 7: '周日'
  }
  
  return `${dayMap[schedule.timeSlot.dayOfWeek]} ${schedule.timeSlot.startTime}-${schedule.timeSlot.endTime}`
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm')
}

// 获取课程状态标签类型
const getStatusTagType = (status: string) => {
  switch (status) {
    case 'OPEN':
      return 'success'
    case 'CLOSED':
      return 'danger'
    case 'PENDING':
      return 'warning'
    default:
      return 'info'
  }
}

// 获取课程状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'OPEN':
      return '开放'
    case 'CLOSED':
      return '关闭'
    case 'PENDING':
      return '待开放'
    default:
      return '未知'
  }
}

// 获取选课状态标签类型
const getSelectionStatusTagType = (status: string) => {
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

// 获取选课状态文本
const getSelectionStatusText = (status: string) => {
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
.semester-selector {
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>