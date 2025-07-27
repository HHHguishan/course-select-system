import { defineStore } from 'pinia'
import { ref, computed, readonly } from 'vue'
import Cookies from 'js-cookie'
import { authApi } from '@/api/auth'
import type { UserInfo, LoginRequest, RegisterRequest } from '@/types/user'
import { UserRole } from '@/types/user'

const TOKEN_KEY = 'access_token'
const REFRESH_TOKEN_KEY = 'refresh_token'
const USER_INFO_KEY = 'user_info'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>('')
  const refreshToken = ref<string>('')
  const userInfo = ref<UserInfo | null>(null)
  const isLoading = ref(false)
  
  // 计算属性
  const isLoggedIn = computed(() => !!token.value && !!userInfo.value)
  const isStudent = computed(() => userInfo.value?.role === UserRole.STUDENT)
  const isTeacher = computed(() => userInfo.value?.role === UserRole.TEACHER)
  const isAdmin = computed(() => userInfo.value?.role === UserRole.ADMIN)
  const userName = computed(() => userInfo.value?.realName || userInfo.value?.username || '')
  
  // 方法
  const setToken = (accessToken: string, refreshTokenValue: string) => {
    token.value = accessToken
    refreshToken.value = refreshTokenValue
    Cookies.set(TOKEN_KEY, accessToken, { expires: 7 })
    Cookies.set(REFRESH_TOKEN_KEY, refreshTokenValue, { expires: 30 })
  }
  
  const setUserInfo = (info: UserInfo) => {
    userInfo.value = info
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(info))
  }
  
  const clearAuthData = () => {
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null
    Cookies.remove(TOKEN_KEY)
    Cookies.remove(REFRESH_TOKEN_KEY)
    localStorage.removeItem(USER_INFO_KEY)
  }
  
  const login = async (loginData: LoginRequest) => {
    isLoading.value = true
    try {
      const response = await authApi.login(loginData)
      setToken(response.accessToken, response.refreshToken)
      setUserInfo(response.userInfo)
      return response
    } finally {
      isLoading.value = false
    }
  }
  
  const register = async (registerData: RegisterRequest) => {
    isLoading.value = true
    try {
      await authApi.register(registerData)
    } finally {
      isLoading.value = false
    }
  }
  
  const logout = async () => {
    try {
      if (token.value) {
        await authApi.logout()
      }
    } catch (error) {
      console.error('登出失败:', error)
    } finally {
      clearAuthData()
    }
  }
  
  const getCurrentUser = async () => {
    if (!token.value) return null
    
    try {
      const userInfo = await authApi.getCurrentUser()
      setUserInfo(userInfo)
      return userInfo
    } catch (error) {
      console.error('获取用户信息失败:', error)
      clearAuthData()
      return null
    }
  }
  
  const refreshAccessToken = async () => {
    if (!refreshToken.value) {
      clearAuthData()
      throw new Error('No refresh token available')
    }
    
    try {
      const response = await authApi.refreshToken(refreshToken.value)
      setToken(response.accessToken, response.refreshToken)
      setUserInfo(response.userInfo)
      return response.accessToken
    } catch (error) {
      clearAuthData()
      throw error
    }
  }
  
  const initUserInfo = () => {
    // 从cookie和localStorage恢复用户信息
    const savedToken = Cookies.get(TOKEN_KEY)
    const savedRefreshToken = Cookies.get(REFRESH_TOKEN_KEY)
    const savedUserInfo = localStorage.getItem(USER_INFO_KEY)
    
    if (savedToken && savedRefreshToken) {
      token.value = savedToken
      refreshToken.value = savedRefreshToken
    }
    
    if (savedUserInfo) {
      try {
        userInfo.value = JSON.parse(savedUserInfo)
      } catch (error) {
        console.error('解析用户信息失败:', error)
        localStorage.removeItem(USER_INFO_KEY)
      }
    }
    
    // 如果有token但没有用户信息，尝试获取用户信息
    if (savedToken && !userInfo.value) {
      getCurrentUser()
    }
  }
  
  return {
    // 状态
    token: readonly(token),
    refreshToken: readonly(refreshToken),
    userInfo: readonly(userInfo),
    isLoading: readonly(isLoading),
    
    // 计算属性
    isLoggedIn,
    isStudent,
    isTeacher,
    isAdmin,
    userName,
    
    // 方法
    login,
    register,
    logout,
    getCurrentUser,
    refreshAccessToken,
    initUserInfo,
    setToken,
    setUserInfo,
    clearAuthData
  }
})