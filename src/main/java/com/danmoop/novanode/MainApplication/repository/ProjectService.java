package com.danmoop.novanode.MainApplication.repository;

import com.danmoop.novanode.MainApplication.model.Project;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProjectService extends MongoRepository<Project, String> {
    Project findByName(String name);

    List<Project> findAll();
}