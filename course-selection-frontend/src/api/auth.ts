import { http } from '@/utils/request'
import type { LoginRequest, LoginResponse, RegisterRequest, UserInfo } from '@/types/user'

export const authApi = {
  // 登录
  login(data: LoginRequest): Promise<LoginResponse> {
    return http.post('/auth/login', data)
  },
  
  // 注册
  register(data: RegisterRequest): Promise<void> {
    return http.post('/auth/register', data)
  },
  
  // 刷新token
  refreshToken(refreshToken: string): Promise<LoginResponse> {
    return http.post('/auth/refresh', null, {
      params: { refreshToken }
    })
  },
  
  // 登出
  logout(): Promise<void> {
    return http.post('/auth/logout')
  },
  
  // 获取当前用户信息
  getCurrentUser(): Promise<UserInfo> {
    return http.get('/auth/me')
  }
}