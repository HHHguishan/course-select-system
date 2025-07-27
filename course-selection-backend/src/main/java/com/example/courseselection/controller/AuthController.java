package com.example.courseselection.controller;

import com.example.courseselection.common.result.Result;
import com.example.courseselection.dto.request.LoginRequest;
import com.example.courseselection.dto.request.RegisterRequest;
import com.example.courseselection.dto.response.LoginResponse;
import com.example.courseselection.security.CustomUserDetails;
import com.example.courseselection.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success("登录成功", response);
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.<Void>success("注册成功", null);
    }

    @PostMapping("/refresh")
    public Result<LoginResponse> refreshToken(@RequestParam String refreshToken) {
        LoginResponse response = authService.refreshToken(refreshToken);
        return Result.success("刷新令牌成功", response);
    }

    @PostMapping("/logout")
    public Result<Void> logout(@AuthenticationPrincipal CustomUserDetails currentUser) {
        // JWT是无状态的，客户端删除token即可实现登出
        // 这里可以实现token黑名单机制
        return Result.<Void>success("登出成功", null);
    }

    @GetMapping("/me")
    public Result<LoginResponse.UserInfo> getCurrentUser(@AuthenticationPrincipal CustomUserDetails currentUser) {
        // 可以通过AuthService重新构建用户信息以获取最新数据
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(currentUser.getId());
        userInfo.setUsername(currentUser.getUsername());
        userInfo.setEmail(currentUser.getEmail());
        userInfo.setRealName(currentUser.getRealName());
        userInfo.setRole(currentUser.getRole());
        
        return Result.success("获取用户信息成功", userInfo);
    }
}