<template>
  <div class="layout-container">
    <el-container class="full-height">
      <!-- 侧边栏 -->
      <el-aside :width="sidebarWidth" class="sidebar">
        <div class="logo">
          <h2>选课系统</h2>
        </div>
        <el-menu
          :default-active="$route.path"
          :collapse="isCollapse"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          router
        >
          <template v-for="route in menuRoutes" :key="route.path">
            <!-- 有子菜单的路由 -->
            <el-sub-menu v-if="route.children && route.children.length > 0" :index="route.path">
              <template #title>
                <el-icon v-if="route.meta?.icon">
                  <component :is="route.meta.icon" />
                </el-icon>
                <span>{{ route.meta?.title }}</span>
              </template>
              <el-menu-item
                v-for="child in route.children"
                :key="child.path"
                :index="child.path"
              >
                <el-icon v-if="child.meta?.icon">
                  <component :is="child.meta.icon" />
                </el-icon>
                <span>{{ child.meta?.title }}</span>
              </el-menu-item>
            </el-sub-menu>
            <!-- 没有子菜单的路由 -->
            <el-menu-item v-else :index="route.path">
              <el-icon v-if="route.meta?.icon">
                <component :is="route.meta.icon" />
              </el-icon>
              <span>{{ route.meta?.title }}</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-container>
        <!-- 顶部导航 -->
        <el-header class="header">
          <div class="header-left">
            <el-button text @click="toggleSidebar">
              <el-icon>
                <Fold v-if="!isCollapse" />
                <Expand v-else />
              </el-icon>
            </el-button>
          </div>
          <div class="header-right">
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <el-icon><User /></el-icon>
                {{ userStore.userName }}
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 主体内容 -->
        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import { UserRole } from '@/types/user'

const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)

const sidebarWidth = computed(() => isCollapse.value ? '64px' : '200px')

// 根据用户角色过滤菜单
const menuRoutes = computed(() => {
  const routes = router.getRoutes()
  return routes.filter(route => {
    // 过滤掉隐藏的菜单和不需要认证的页面
    if (route.meta?.hideInMenu || !route.meta?.requireAuth) {
      return false
    }
    
    // 检查角色权限
    if (route.meta?.roles && route.meta.roles.length > 0) {
      const userRole = userStore.userInfo?.role
      return userRole && route.meta.roles.includes(userRole)
    }
    
    return true
  }).map(route => {
    // 处理子路由
    if (route.children) {
      const validChildren = route.children.filter(child => {
        if (child.meta?.hideInMenu) return false
        if (child.meta?.roles && child.meta.roles.length > 0) {
          const userRole = userStore.userInfo?.role
          return userRole && child.meta.roles.includes(userRole)
        }
        return true
      })
      return {
        ...route,
        children: validChildren
      }
    }
    return route
  })
})

const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      // TODO: 打开个人信息对话框
      break
    case 'logout':
      try {
        await ElMessageBox.confirm(
          '确定要退出登录吗？',
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        await userStore.logout()
        router.push('/login')
      } catch {
        // 用户取消
      }
      break
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background-color: #304156;
  transition: width 0.3s;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2b3a4b;
  color: white;
  margin-bottom: 10px;
}

.logo h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.header {
  background-color: white;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.user-info .el-icon {
  margin: 0 4px;
}

.main-content {
  background-color: #f0f2f5;
  padding: 0;
}

:deep(.el-menu) {
  border-right: none;
}

:deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
}

:deep(.el-sub-menu .el-sub-menu__title) {
  height: 50px;
  line-height: 50px;
}
</style>