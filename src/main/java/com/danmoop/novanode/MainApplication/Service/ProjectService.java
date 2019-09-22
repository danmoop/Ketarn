package com.danmoop.novanode.MainApplication.Service;

import com.danmoop.novanode.MainApplication.Model.Project;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProjectService extends MongoRepository<Project, String> {
    Project findByName(String name);

    List<Project> findAll();
}