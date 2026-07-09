package hgc.flowsyncapi.service;

import hgc.flowsyncapi.entity.User;

public interface AuthService {
    User login(String username, String password);
    boolean register(User user);
}
