<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">课程表</h1>
      <p class="page-description">查看您的个人课程安排</p>
    </div>

    <div class="card">
      <div class="table-header">
        <div class="semester-selector">
          <el-select v-model="selectedSemester" placeholder="选择学期" @change="loadSchedule">
            <el-option label="当前学期" value="current" />
            <el-option label="2024春季学期" value="2024-spring" />
            <el-option label="2023秋季学期" value="2023-fall" />
          </el-select>
        </div>
        <el-button @click="loadSchedule">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>

      <div v-loading="loading" class="schedule-container">
        <div class="schedule-grid">
          <!-- 时间表头 -->
          <div class="time-header">时间</div>
          <div v-for="day in weekDays" :key="day.value" class="day-header">
            {{ day.label }}
          </div>

          <!-- 时间行 -->
          <template v-for="time in timeSlots" :key="time.key">
            <div class="time-cell">
              {{ time.label }}
            </div>
            <div 
              v-for="day in weekDays" 
              :key="`${time.key}-${day.value}`" 
              class="schedule-cell"
              :class="{ 'has-course': getCourseByTime(day.value, time.key) }"
            >
              <div 
                v-if="getCourseByTime(day.value, time.key)" 
                class="course-card"
                @click="showCourseDetail(getCourseByTime(day.value, time.key))"
              >
                <div class="course-name">
                  {{ getCourseByTime(day.value, time.key)?.courseSchedule.course.courseName }}
                </div>
                <div class="course-info">
                  {{ getCourseByTime(day.value, time.key)?.courseSchedule.teacher?.realName }}
                </div>
                <div class="course-info">
                  {{ getCourseByTime(day.value, time.key)?.courseSchedule.classroom }}
                </div>
              </div>
            </div>
          </template>
        </div>
      </div>

      <!-- 课程详情对话框 -->
      <el-dialog
        v-model="detailDialogVisible"
        title="课程详情"
        width="500px"
      >
        <el-descriptions v-if="selectedCourse" :column="1" border>
          <el-descriptions-item label="课程名称">
            {{ selectedCourse.courseSchedule.course.courseName }}
          </el-descriptions-item>
          <el-descriptions-item label="课程代码">
            {{ selectedCourse.courseSchedule.course.courseCode }}
          </el-descriptions-item>
          <el-descriptions-item label="授课教师">
            {{ selectedCourse.courseSchedule.teacher?.realName }}
          </el-descriptions-item>
          <el-descriptions-item label="学分">
            {{ selectedCourse.courseSchedule.course.credits }}
          </el-descriptions-item>
          <el-descriptions-item label="教室">
            {{ selectedCourse.courseSchedule.classroom }}
          </el-descriptions-item>
          <el-descriptions-item label="上课时间">
            {{ formatClassTime(selectedCourse.courseSchedule) }}
          </el-descriptions-item>
          <el-descriptions-item label="选课状态">
            <el-tag :type="getStatusTagType(selectedCourse.status)">
              {{ getStatusText(selectedCourse.status) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { courseApi } from '@/api/course'
import { ElMessage } from 'element-plus'
import type { CourseSelection, CourseSchedule } from '@/types/course'

const loading = ref(false)
const selectedSemester = ref('current')
const detailDialogVisible = ref(false)
const selectedCourse = ref<CourseSelection | null>(null)

const courseList = ref<CourseSelection[]>([])

// 星期数据
const weekDays = [
  { label: '周一', value: 1 },
  { label: '周二', value: 2 },
  { label: '周三', value: 3 },
  { label: '周四', value: 4 },
  { label: '周五', value: 5 },
  { label: '周六', value: 6 },
  { label: '周日', value: 7 }
]

// 时间段数据
const timeSlots = [
  { key: '1-2', label: '08:00-09:40' },
  { key: '3-4', label: '10:00-11:40' },
  { key: '5-6', label: '14:00-15:40' },
  { key: '7-8', label: '16:00-17:40' },
  { key: '9-10', label: '19:00-20:40' }
]

// 加载课程表数据
const loadSchedule = async () => {
  loading.value = true
  try {
    // 这里简化处理，实际应该根据学期参数获取
    const response = await courseApi.getMySelectedCourses({ page: 1, size: 100 })
    courseList.value = response.content.filter(course => course.status === 'CONFIRMED')
  } catch (error) {
    console.error('加载课程表失败:', error)
    ElMessage.error('加载课程表失败')
  } finally {
    loading.value = false
  }
}

// 根据时间获取课程
const getCourseByTime = (dayOfWeek: number, timeKey: string) => {
  return courseList.value.find(course => {
    const timeSlot = course.courseSchedule.timeSlot
    if (!timeSlot) return false
    
    // 这里简化处理，实际应该根据具体的时间段规则匹配
    return timeSlot.dayOfWeek === dayOfWeek && 
           timeSlot.startTime <= timeKey.split('-')[0] && 
           timeSlot.endTime >= timeKey.split('-')[1]
  })
}

// 显示课程详情
const showCourseDetail = (course: CourseSelection | undefined) => {
  if (course) {
    selectedCourse.value = course
    detailDialogVisible.value = true
  }
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
  loadSchedule()
})
</script>

<style scoped>
.semester-selector {
  display: flex;
  align-items: center;
  gap: 12px;
}

.schedule-container {
  margin-top: 20px;
  overflow-x: auto;
}

.schedule-grid {
  display: grid;
  grid-template-columns: 100px repeat(7, 1fr);
  gap: 1px;
  background-color: #e5e7eb;
  border: 1px solid #e5e7eb;
  min-width: 800px;
}

.time-header,
.day-header {
  background-color: #f3f4f6;
  padding: 12px 8px;
  text-align: center;
  font-weight: 600;
  font-size: 14px;
}

.time-cell {
  background-color: #f9fafb;
  padding: 8px;
  text-align: center;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 80px;
}

.schedule-cell {
  background-color: white;
  min-height: 80px;
  position: relative;
}

.schedule-cell.has-course {
  padding: 4px;
}

.course-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 8px;
  border-radius: 6px;
  cursor: pointer;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  transition: transform 0.2s, box-shadow 0.2s;
}

.course-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.course-name {
  font-weight: 600;
  font-size: 13px;
  margin-bottom: 4px;
  line-height: 1.2;
}

.course-info {
  font-size: 11px;
  opacity: 0.9;
  line-height: 1.2;
}

@media (max-width: 768px) {
  .schedule-grid {
    font-size: 12px;
  }
  
  .course-name {
    font-size: 11px;
  }
  
  .course-info {
    font-size: 10px;
  }
  
  .time-cell {
    font-size: 10px;
    min-height: 60px;
  }
  
  .schedule-cell {
    min-height: 60px;
  }
}
</style>