package com.danmoop.novanode.MainApplication.repository;

import com.danmoop.novanode.MainApplication.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserService extends MongoRepository<User, String> {
    User findByUserName(String userName);

    List<User> findAll();
}
