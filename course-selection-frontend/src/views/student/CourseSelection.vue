<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">选课中心</h1>
      <p class="page-description">在这里您可以浏览和选择感兴趣的课程</p>
    </div>

    <div class="card">
      <div class="table-header">
        <div>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索课程名称或代码"
            style="width: 300px; margin-right: 16px;"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" @click="searchCourses">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
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
            {{ scope.row.courseCode }}
          </template>
        </el-table-column>
        <el-table-column label="课程名称" min-width="200">
          <template #default="scope">
            {{ scope.row.courseName }}
          </template>
        </el-table-column>
        <el-table-column label="授课教师" width="120">
          <template #default="scope">
            {{ scope.row.teacherName }}
          </template>
        </el-table-column>
        <el-table-column label="学分" width="80">
          <template #default="scope">
            {{ scope.row.credits }}
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
            <span>{{ scope.row.currentStudents }}/{{ scope.row.maxStudents }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row)">
              {{ getStatusText(scope.row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button
              v-if="!scope.row.isSelected && canSelectCourse(scope.row)"
              type="primary"
              size="small"
              @click="selectCourse(scope.row)"
            >
              选课
            </el-button>
            <el-button
              v-else-if="scope.row.isSelected"
              type="danger"
              size="small"
              @click="dropCourse(scope.row)"
            >
              退课
            </el-button>
            <el-button
              v-else
              size="small"
              disabled
            >
              不可选
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { courseApi } from '@/api/course'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { CourseSchedule } from '@/types/course'

const loading = ref(false)
const searchKeyword = ref('')

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const courseList = ref<CourseSchedule[]>([])

// 搜索和获取课程数据
const loadCourses = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      keyword: searchKeyword.value || undefined
    }
    const response = await courseApi.getAvailableCourses(params)
    courseList.value = response.content
    pagination.total = response.totalElements
  } catch (error) {
    console.error('加载课程失败:', error)
    ElMessage.error('加载课程数据失败')
  } finally {
    loading.value = false
  }
}

const searchCourses = () => {
  pagination.page = 1
  loadCourses()
}

const refreshCourses = () => {
  searchKeyword.value = ''
  pagination.page = 1
  loadCourses()
}

const selectCourse = async (courseSchedule: CourseSchedule) => {
  try {
    await ElMessageBox.confirm(
      `确定要选择课程《${courseSchedule.courseName}》吗？`,
      '确认选课',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'question'
      }
    )
    
    await courseApi.selectCourse({ courseScheduleId: courseSchedule.id })
    ElMessage.success(`成功选择课程：${courseSchedule.courseName}`)
    
    // 刷新数据
    loadCourses()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('选课失败:', error)
      ElMessage.error(error.message || '选课失败')
    }
  }
}

const dropCourse = async (courseSchedule: CourseSchedule) => {
  try {
    await ElMessageBox.confirm(
      `确定要退选课程《${courseSchedule.courseName}》吗？`,
      '确认退课',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await courseApi.dropCourse(courseSchedule.id)
    ElMessage.success(`成功退选课程：${courseSchedule.courseName}`)
    
    // 刷新数据
    loadCourses()
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
  loadCourses()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  loadCourses()
}

// 格式化上课时间
const formatClassTime = (schedule: CourseSchedule) => {
  if (!schedule.scheduleTime || schedule.scheduleTime.length === 0) return '待安排'
  
  const dayMap: Record<number, string> = {
    1: '周一', 2: '周二', 3: '周三', 4: '周四', 
    5: '周五', 6: '周六', 7: '周日'
  }
  
  const timeSlot = schedule.scheduleTime[0]
  return `${dayMap[timeSlot.dayOfWeek]} ${timeSlot.startTime}-${timeSlot.endTime}`
}

// 判断课程是否可选
const canSelectCourse = (schedule: CourseSchedule) => {
  return schedule.status === 'OPEN' && 
         schedule.currentStudents < schedule.maxStudents &&
         !schedule.isSelected
}

// 获取课程状态标签类型
const getStatusTagType = (schedule: CourseSchedule) => {
  if (schedule.isSelected) return 'success'
  if (canSelectCourse(schedule)) return 'primary'
  return 'danger'
}

// 获取课程状态文本
const getStatusText = (schedule: CourseSchedule) => {
  if (schedule.isSelected) return '已选'
  if (schedule.status !== 'OPEN') return '未开放'
  if (schedule.currentStudents >= schedule.maxStudents) return '已满'
  return '可选'
}

onMounted(() => {
  loadCourses()
})
</script>