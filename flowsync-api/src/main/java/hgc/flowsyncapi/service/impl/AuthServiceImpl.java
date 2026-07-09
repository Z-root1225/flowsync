package hgc.flowsyncapi.service.impl;

import hgc.flowsyncapi.entity.User;
import hgc.flowsyncapi.service.AuthService;
import hgc.flowsyncapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserService userService;

    @Override
    public User login(String username, String password) {
        User user = userService.getByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            User scrubbedUser = new User();
            scrubbedUser.setId(user.getId());
            scrubbedUser.setUsername(user.getUsername());
            scrubbedUser.setRealName(user.getRealName());
            scrubbedUser.setRole(user.getRole());
            scrubbedUser.setPhone(user.getPhone());
            scrubbedUser.setEmail(user.getEmail());
            scrubbedUser.setCreateTime(user.getCreateTime());
            return scrubbedUser; // 返回剔除了密码的安全用户信息
        }
        return null;
    }

    @Override
    public boolean register(User user) {
        User existingUser = userService.getByUsername(user.getUsername());
        if (existingUser != null) {
            return false; // 用户名已冲突
        }
        return userService.save(user);
    }
}
