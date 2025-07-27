package com.example.courseselection.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "方法不允许"),
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    
    // 用户相关
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    USERNAME_OR_PASSWORD_ERROR(1003, "用户名或密码错误"),
    USER_DISABLED(1004, "用户已被禁用"),
    TOKEN_EXPIRED(1005, "Token已过期"),
    TOKEN_INVALID(1006, "Token无效"),
    
    // 课程相关
    COURSE_NOT_FOUND(2001, "课程不存在"),
    COURSE_ALREADY_EXISTS(2002, "课程已存在"),
    COURSE_FULL(2003, "课程人数已满"),
    COURSE_NOT_OPEN(2004, "课程未开放选课"),
    COURSE_SELECTION_CLOSED(2005, "选课时间已结束"),
    
    // 选课相关
    ALREADY_SELECTED(3001, "已选择该课程"),
    NOT_SELECTED(3002, "未选择该课程"),
    SCHEDULE_CONFLICT(3003, "课程时间冲突"),
    CREDITS_EXCEEDED(3004, "超出最大学分限制"),
    PREREQUISITE_NOT_MET(3005, "不满足前置课程要求"),
    DROP_TIME_EXPIRED(3006, "退课时间已过"),
    
    // 权限相关
    PERMISSION_DENIED(4001, "权限不足"),
    ROLE_NOT_ALLOWED(4002, "角色不允许此操作");

    private final Integer code;
    private final String message;
}