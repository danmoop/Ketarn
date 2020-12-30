package com.danmoop.novanode.MainApplication.service;

import com.danmoop.novanode.MainApplication.model.User;
import com.danmoop.novanode.MainApplication.service.interfaces.UserInterface;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class UserService implements UserInterface {

    @Autowired
    private com.danmoop.novanode.MainApplication.repository.UserService userService;

    @Override
    public User findByUserName(String userName) {
        return userService.findByUserName(userName);
    }

    @Override
    public List<User> findAll() {
        return userService.findAll();
    }

    @Override
    public void save(User user) {
        userService.save(user);
    }

    @Override
    public void delete(User user) {
        userService.delete(user);
    }
}
