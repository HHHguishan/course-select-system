# 学生选课系统前端

基于 Vue 3 + TypeScript + Vite + Element Plus 构建的现代化学生选课系统前端。

## 技术栈

- **Vue 3** - 渐进式JavaScript框架
- **TypeScript** - JavaScript的超集，提供类型安全
- **Vite** - 下一代前端构建工具
- **Element Plus** - Vue 3的UI组件库
- **Vue Router** - Vue.js官方路由管理器
- **Pinia** - Vue的状态管理库
- **Axios** - 基于Promise的HTTP客户端

## 项目结构

```
src/
├── api/                    # API接口定义
│   ├── auth.ts            # 认证相关接口
│   └── course.ts          # 课程相关接口
├── components/            # 公共组件
├── layout/               # 布局组件
├── router/               # 路由配置
├── stores/               # Pinia状态管理
│   └── user.ts           # 用户状态管理
├── styles/               # 全局样式
├── types/                # TypeScript类型定义
│   ├── api.ts            # API相关类型
│   ├── course.ts         # 课程相关类型
│   └── user.ts           # 用户相关类型
├── utils/                # 工具函数
│   └── request.ts        # HTTP请求封装
├── views/                # 页面组件
│   ├── student/          # 学生功能页面
│   ├── teacher/          # 教师功能页面
│   ├── admin/            # 管理员功能页面
│   ├── Login.vue         # 登录页面
│   ├── Register.vue      # 注册页面
│   ├── Dashboard.vue     # 首页
│   └── 404.vue           # 404页面
├── App.vue               # 根组件
└── main.ts               # 应用入口
```

## 功能模块

### 用户角色
- **学生** - 选课、退课、查看课程表
- **教师** - 查看授课课程、学生名单
- **管理员** - 课程管理、学生管理、系统配置

### 核心功能
- 用户登录/注册/登出
- 基于角色的权限控制
- 学生选课中心
- 个人课程管理
- 课程表显示
- 管理员后台

## 开发指南

### 环境要求
- Node.js >= 16.0.0
- npm >= 8.0.0

### 安装依赖
```bash
npm install
```

### 开发模式
```bash
npm run dev
```

### 构建生产版本
```bash
npm run build
```

### 预览生产版本
```bash
npm run preview
```

### 代码检查
```bash
npm run lint
```

### 类型检查
```bash
npm run type-check
```

## 配置说明

### 环境变量
- `.env` - 开发环境配置
- `.env.production` - 生产环境配置

### 主要配置项
- `VITE_APP_TITLE` - 应用标题
- `VITE_API_BASE_URL` - API基础URL
- `VITE_APP_ENV` - 应用环境

### 代理配置
开发环境下，所有 `/api` 开头的请求会被代理到后端服务器 `http://localhost:8080`

## 路由设计

- `/login` - 登录页面
- `/register` - 注册页面
- `/dashboard` - 首页
- `/student/*` - 学生功能模块
- `/teacher/*` - 教师功能模块
- `/admin/*` - 管理员功能模块

## 状态管理

使用 Pinia 进行状态管理，主要包括：
- 用户状态管理（登录状态、用户信息、权限等）
- 自动token刷新机制
- 持久化存储（Cookie + LocalStorage）

## 样式规范

- 使用 Element Plus 提供的设计规范
- 支持暗黑模式
- 响应式设计，适配移动端
- 提供丰富的工具类

## API接口

与后端API无缝对接，包括：
- 自动token认证
- 请求/响应拦截器
- 错误处理机制
- 统一响应格式

## 部署说明

1. 构建生产版本：`npm run build`
2. 将 `dist` 目录部署到Web服务器
3. 配置Nginx代理后端API

## 开发规范

- 使用TypeScript严格模式
- 遵循Vue 3 Composition API规范
- 组件采用单文件组件（SFC）格式
- 使用ESLint + Prettier代码格式化