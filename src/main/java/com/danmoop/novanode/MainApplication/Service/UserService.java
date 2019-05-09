package com.danmoop.novanode.MainApplication.Service;

import com.danmoop.novanode.MainApplication.Model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserService extends MongoRepository<User, String>
{
    User findByEmail(String email);
    User findByUserName(String userName);
    User findByName(String name);

    List<User> findAll();
}
