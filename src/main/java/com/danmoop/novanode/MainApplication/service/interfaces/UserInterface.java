package com.danmoop.novanode.MainApplication.service.interfaces;

import com.danmoop.novanode.MainApplication.model.User;

import java.util.List;

public interface UserInterface {
    User findByUserName(String userName);

    List<User> findAll();

    void save(User user);

    void delete(User user);
}