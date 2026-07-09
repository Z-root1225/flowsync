package hgc.flowsyncapi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import hgc.flowsyncapi.entity.User;
import java.util.List;

public interface UserService extends IService<User> {
    List<User> listUsersWithoutPassword();
    User getByUsername(String username);
}
