package hgc.flowsyncapi.controller;

import hgc.flowsyncapi.common.ApiResponse;
import hgc.flowsyncapi.dto.PasswordUpdateRequest;
import hgc.flowsyncapi.entity.User;
import hgc.flowsyncapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // 获取全部用户列表 (不含密码)
    @GetMapping
    public ApiResponse<List<User>> getUsers() {
        List<User> users = userService.listUsersWithoutPassword();
        return ApiResponse.success(users);
    }

    // 修改密码 (当前用户 ID 由 query 参数 currentUserId 携带)
    @PostMapping("/update-password")
    public ApiResponse<String> updatePassword(
            @RequestParam("currentUserId") Long currentUserId,
            @RequestBody PasswordUpdateRequest request) {
        
        User user = userService.getById(currentUserId);
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }

        if (!user.getPassword().equals(request.getOldPassword())) {
            return ApiResponse.error("原密码输入错误，校验失败");
        }

        user.setPassword(request.getNewPassword());
        boolean success = userService.updateById(user);
        if (success) {
            return ApiResponse.success("密码更新成功", "Password updated successfully");
        } else {
            return ApiResponse.error("更新密码数据库异常，请稍后再试");
        }
    }
}
