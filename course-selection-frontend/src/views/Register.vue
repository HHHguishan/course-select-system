<template>
  <div class="register-container">
    <div class="register-form">
      <div class="register-header">
        <h2>用户注册</h2>
        <p>创建新账号</p>
      </div>
      
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名" />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="registerForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="registerForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        
        <el-form-item label="角色" prop="role">
          <el-select v-model="registerForm.role" placeholder="请选择角色">
            <el-option label="学生" value="STUDENT" />
            <el-option label="教师" value="TEACHER" />
          </el-select>
        </el-form-item>
        
        <!-- 学生专用字段 -->
        <template v-if="registerForm.role === 'STUDENT'">
          <el-form-item label="学号" prop="studentNumber">
            <el-input v-model="registerForm.studentNumber" placeholder="请输入学号" />
          </el-form-item>
          <el-form-item label="专业" prop="major">
            <el-input v-model="registerForm.major" placeholder="请输入专业" />
          </el-form-item>
        </template>
        
        <!-- 教师专用字段 -->
        <template v-if="registerForm.role === 'TEACHER'">
          <el-form-item label="工号" prop="teacherNumber">
            <el-input v-model="registerForm.teacherNumber" placeholder="请输入工号" />
          </el-form-item>
          <el-form-item label="院系" prop="department">
            <el-input v-model="registerForm.department" placeholder="请输入院系" />
          </el-form-item>
        </template>
        
        <el-form-item>
          <el-button
            type="primary"
            :loading="userStore.isLoading"
            @click="handleRegister"
            style="width: 100%"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="register-footer">
        <el-button type="text" @click="$router.push('/login')">
          已有账号？立即登录
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { UserRole } from '@/types/user'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const registerFormRef = ref<FormInstance>()
const registerForm = reactive({
  username: '',
  password: '',
  realName: '',
  email: '',
  role: '' as UserRole | '',
  studentNumber: '',
  major: '',
  teacherNumber: '',
  department: ''
})

const registerRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '用户名长度应为4-20位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await userStore.register(registerForm as any)
        ElMessage.success('注册成功，请登录')
        router.push('/login')
      } catch (error) {
        console.error('注册失败:', error)
      }
    }
  })
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.register-form {
  width: 500px;
  max-width: 90vw;
  background: white;
  border-radius: 8px;
  padding: 40px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h2 {
  color: #303133;
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 8px;
}

.register-header p {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

.register-footer {
  text-align: center;
  margin-top: 20px;
}
</style>