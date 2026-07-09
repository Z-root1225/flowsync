package hgc.flowsyncapi.controller;

import hgc.flowsyncapi.common.ApiResponse;
import hgc.flowsyncapi.dto.LoginRequest;
import hgc.flowsyncapi.entity.User;
import hgc.flowsyncapi.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ApiResponse<User> login(@RequestBody LoginRequest request) {
        User user = authService.login(request.getUsername(), request.getPassword());
        if (user != null) {
            return ApiResponse.success("登录成功", user);
        } else {
            return ApiResponse.error("账号或密码错误");
        }
    }

    @PostMapping("/register")
    public ApiResponse<String> register(@RequestBody User user) {
        // 注册新用户，默认注册时间
        user.setCreateTime(java.time.LocalDateTime.now());
        boolean success = authService.register(user);
        if (success) {
            return ApiResponse.success("注册成功", "User registered successfully");
        } else {
            return ApiResponse.error("用户名已存在，请重新输入");
        }
    }
}
