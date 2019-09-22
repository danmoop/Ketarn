package com.danmoop.novanode.MainApplication.Service;

import com.danmoop.novanode.MainApplication.Model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserService extends MongoRepository<User, String> {
    User findByUserName(String userName);

    List<User> findAll();
}
