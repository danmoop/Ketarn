package com.danmoop.novanode.MainApplication.service;

import com.danmoop.novanode.MainApplication.model.Project;
import com.danmoop.novanode.MainApplication.service.interfaces.ProjectInterface;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ProjectService implements ProjectInterface {

    @Autowired
    private com.danmoop.novanode.MainApplication.repository.ProjectService projectService;

    @Override
    public Project findByName(String name) {
        return projectService.findByName(name);
    }

    @Override
    public List<Project> findAll() {
        return projectService.findAll();
    }
}
