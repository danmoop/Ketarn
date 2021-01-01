package com.danmoop.novanode.MainApplication.service;

import com.danmoop.novanode.MainApplication.model.User;
import com.danmoop.novanode.MainApplication.repository.UserRepository;
import com.danmoop.novanode.MainApplication.service.interfaces.UserInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements UserInterface {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User findByUserName(String userName) {
        return userRepository.findByUserName(userName);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public void readAllInbox(String username) {
        User userDB = userRepository.findByUserName(username); // find user in database by username

        userDB.readAllInboxMessages(); // empty messages array
        save(userDB); // save user to database
    }

    @Override
    public void deleteAllInbox(String username) {
        User userDB = userRepository.findByUserName(username);

        userDB.getReadMessages().clear();
        save(userDB);
    }

    @Override
    public void editUserNotes(String username, String noteText) {
        User userDB = userRepository.findByUserName(username);

        if (userDB != null) {
            userDB.setNote(noteText);
            save(userDB);
        }
    }

    @Override
    public void save(User user) {
        userRepository.save(user);
    }

    @Override
    public void delete(User user) {
        userRepository.delete(user);
    }
}