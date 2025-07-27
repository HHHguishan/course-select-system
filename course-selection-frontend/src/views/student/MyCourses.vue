<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">我的课程</h1>
      <p class="page-description">查看您已选择的课程信息</p>
    </div>

    <div class="card">
      <div class="table-header">
        <div class="stats-info">
          <el-tag type="success">
            <el-icon><Document /></el-icon>
            已选课程: {{ pagination.total }} 门
          </el-tag>
          <el-tag type="info">
            <el-icon><Star /></el-icon>
            总学分: {{ totalCredits }}
          </el-tag>
        </div>
        <el-button @click="refreshCourses">
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
            {{ scope.row.courseSchedule.course.courseCode }}
          </template>
        </el-table-column>
        <el-table-column label="课程名称" min-width="200">
          <template #default="scope">
            {{ scope.row.courseSchedule.course.courseName }}
          </template>
        </el-table-column>
        <el-table-column label="授课教师" width="120">
          <template #default="scope">
            {{ scope.row.courseSchedule.teacher?.realName }}
          </template>
        </el-table-column>
        <el-table-column label="学分" width="80">
          <template #default="scope">
            {{ scope.row.courseSchedule.course.credits }}
          </template>
        </el-table-column>
        <el-table-column prop="courseSchedule.classroom" label="教室" width="120" />
        <el-table-column label="上课时间" width="150">
          <template #default="scope">
            {{ formatClassTime(scope.row.courseSchedule) }}
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
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 'CONFIRMED'"
              type="danger"
              size="small"
              @click="dropCourse(scope.row)"
            >
              退课
            </el-button>
            <span v-else class="text-gray-500">无法操作</span>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { courseApi } from '@/api/course'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { CourseSelection, CourseSchedule } from '@/types/course'
import dayjs from 'dayjs'

const loading = ref(false)

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const courseList = ref<CourseSelection[]>([])

// 计算总学分
const totalCredits = computed(() => {
  return courseList.value.reduce((sum, selection) => {
    return sum + (selection.courseSchedule.course.credits || 0)
  }, 0)
})

// 加载我的课程
const loadMyCourses = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size
    }
    const response = await courseApi.getMySelectedCourses(params)
    courseList.value = response.content
    pagination.total = response.totalElements
  } catch (error) {
    console.error('加载我的课程失败:', error)
    ElMessage.error('加载课程数据失败')
  } finally {
    loading.value = false
  }
}

const refreshCourses = () => {
  loadMyCourses()
}

const dropCourse = async (selection: CourseSelection) => {
  try {
    await ElMessageBox.confirm(
      `确定要退选课程《${selection.courseSchedule.course.courseName}》吗？`,
      '确认退课',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await courseApi.dropCourse(selection.courseSchedule.id)
    ElMessage.success(`成功退选课程：${selection.courseSchedule.course.courseName}`)
    
    // 刷新数据
    loadMyCourses()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('退课失败:', error)
      ElMessage.error(error.message || '退课失败')
    }
  }
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  loadMyCourses()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  loadMyCourses()
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
  loadMyCourses()
})
</script>

<style scoped>
.stats-info {
  display: flex;
  gap: 12px;
  align-items: center;
}

.stats-info .el-tag {
  display: flex;
  align-items: center;
  gap: 4px;
}

.text-gray-500 {
  color: #9ca3af;
  font-size: 14px;
}
</style>