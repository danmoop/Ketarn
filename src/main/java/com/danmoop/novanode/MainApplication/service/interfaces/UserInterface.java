package com.danmoop.novanode.MainApplication.service.interfaces;

import com.danmoop.novanode.MainApplication.model.User;

import java.util.List;

public interface UserInterface {
    User findByUserName(String userName);

    List<User> findAll();

    void readAllInbox(String username);

    void deleteAllInbox(String username);

    void editUserNotes(String username, String noteText);

    void save(User user);

    void delete(User user);
}