# 学生选课系统后端

基于Spring Boot 3.2.0开发的学生选课系统后端服务。

## 技术栈

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Security 6.x** - 安全认证
- **Spring Data JPA** - 数据访问层
- **MySQL 8.0** - 数据库
- **JWT** - Token认证
- **Lombok** - 简化代码
- **MapStruct** - DTO映射
- **Maven** - 项目管理

## 项目结构

```
src/main/java/com/example/courseselection/
├── CourseSelectionApplication.java          # 启动类
├── common/                                   # 公共模块
│   ├── exception/                           # 异常处理
│   │   ├── BusinessException.java          # 业务异常
│   │   └── GlobalExceptionHandler.java     # 全局异常处理器
│   └── result/                              # 统一返回结果
│       ├── Result.java                      # 统一返回结果类
│       └── ResultCode.java                  # 结果状态码
├── config/                                  # 配置类
│   └── CorsConfig.java                      # 跨域配置
├── entity/                                  # 实体类
├── dto/                                     # 数据传输对象
├── repository/                              # 数据访问层
├── service/                                 # 业务逻辑层
├── controller/                              # 控制器层
└── security/                                # 安全配置
```

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 2. 数据库配置

1. 创建数据库：
```sql
CREATE DATABASE course_selection_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行建表脚本：`course_selection_database.sql`

3. 修改配置文件 `application.yml` 中的数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/course_selection_system
    username: your_username
    password: your_password
```

### 3. 运行项目

```bash
# 编译项目
mvn clean compile

# 运行项目
mvn spring-boot:run

# 或者打包后运行
mvn clean package
java -jar target/course-selection-backend-1.0.0.jar
```

### 4. 访问接口

- 项目地址：http://localhost:8080/api
- 接口文档：待完善

## 开发规范

### 1. 代码规范
- 使用Lombok减少样板代码
- 统一使用Result类包装返回结果
- 业务异常使用BusinessException
- 参数校验使用Jakarta Validation

### 2. 分层架构
- Controller层：接收请求，参数校验，调用Service
- Service层：业务逻辑处理，事务控制
- Repository层：数据访问，使用Spring Data JPA

### 3. 命名规范
- 类名：大驼峰命名法（PascalCase）
- 方法名和变量名：小驼峰命名法（camelCase）
- 常量：全大写，下划线分隔
- 包名：全小写

## 待开发功能

- [ ] 用户认证与授权
- [ ] 课程管理
- [ ] 学生管理
- [ ] 选课业务逻辑
- [ ] 成绩管理
- [ ] 系统配置管理