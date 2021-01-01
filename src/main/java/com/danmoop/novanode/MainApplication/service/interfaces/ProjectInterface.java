package com.danmoop.novanode.MainApplication.service.interfaces;

import com.danmoop.novanode.MainApplication.model.Project;

import java.util.List;

public interface ProjectInterface {
    Project findByName(String name);

    List<Project> findAll();

    void save(Project project);

    void delete(Project project);
}