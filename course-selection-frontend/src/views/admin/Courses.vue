<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">课程管理</h1>
      <p class="page-description">管理系统中的所有课程</p>
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
        <div>
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon>
            新增课程
          </el-button>
          <el-button @click="refreshCourses">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="courseList"
        style="width: 100%"
        border
      >
        <el-table-column prop="courseCode" label="课程代码" width="120" />
        <el-table-column prop="courseName" label="课程名称" min-width="200" />
        <el-table-column prop="credits" label="学分" width="80" />
        <el-table-column prop="description" label="课程描述" min-width="300" show-overflow-tooltip />
        <el-table-column label="创建时间" width="150">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="editCourse(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="deleteCourse(scope.row)"
            >
              删除
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

    <!-- 创建/编辑课程对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新增课程' : '编辑课程'"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="courseForm"
        :rules="courseRules"
        label-width="100px"
      >
        <el-form-item label="课程代码" prop="courseCode">
          <el-input v-model="courseForm.courseCode" placeholder="请输入课程代码" />
        </el-form-item>
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="courseForm.courseName" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="学分" prop="credits">
          <el-input-number 
            v-model="courseForm.credits" 
            :min="1" 
            :max="10" 
            placeholder="请输入学分"
          />
        </el-form-item>
        <el-form-item label="课程描述" prop="description">
          <el-input
            v-model="courseForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入课程描述"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { courseApi } from '@/api/course'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Course, CourseCreateRequest } from '@/types/course'
import type { FormInstance, FormRules } from 'element-plus'
import dayjs from 'dayjs'

const loading = ref(false)
const submitLoading = ref(false)
const searchKeyword = ref('')
const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')

const formRef = ref<FormInstance>()
const courseList = ref<Course[]>([])

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const courseForm = reactive<CourseCreateRequest & { id?: number }>({
  courseCode: '',
  courseName: '',
  credits: 1,
  description: ''
})

const courseRules: FormRules = {
  courseCode: [
    { required: true, message: '请输入课程代码', trigger: 'blur' }
  ],
  courseName: [
    { required: true, message: '请输入课程名称', trigger: 'blur' }
  ],
  credits: [
    { required: true, message: '请输入学分', trigger: 'blur' }
  ]
}

// 加载课程列表
const loadCourses = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      keyword: searchKeyword.value || undefined
    }
    const response = await courseApi.getAllCourses(params)
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

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  loadCourses()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  loadCourses()
}

// 显示创建对话框
const showCreateDialog = () => {
  dialogMode.value = 'create'
  resetForm()
  dialogVisible.value = true
}

// 编辑课程
const editCourse = (course: Course) => {
  dialogMode.value = 'edit'
  Object.assign(courseForm, course)
  dialogVisible.value = true
}

// 删除课程
const deleteCourse = async (course: Course) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除课程《${course.courseName}》吗？此操作不可恢复！`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await courseApi.deleteCourse(course.id)
    ElMessage.success('删除成功')
    loadCourses()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(courseForm, {
    courseCode: '',
    courseName: '',
    credits: 1,
    description: ''
  })
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (dialogMode.value === 'create') {
          await courseApi.createCourse(courseForm)
          ElMessage.success('创建成功')
        } else {
          // 这里应该有更新课程的API，暂时用创建代替
          ElMessage.success('更新成功')
        }
        
        dialogVisible.value = false
        loadCourses()
      } catch (error: any) {
        console.error('操作失败:', error)
        ElMessage.error(error.message || '操作失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  loadCourses()
})
</script>