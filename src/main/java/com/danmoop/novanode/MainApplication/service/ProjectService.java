package com.danmoop.novanode.MainApplication.service;

import com.danmoop.novanode.MainApplication.model.Project;
import com.danmoop.novanode.MainApplication.repository.ProjectRepository;
import com.danmoop.novanode.MainApplication.service.interfaces.ProjectInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService implements ProjectInterface {

    @Autowired
    private ProjectRepository projectRepository;

    @Override
    public Project findByName(String name) {
        return projectRepository.findByName(name);
    }

    @Override
    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    @Override
    public void save(Project project) {
        projectRepository.save(project);
    }

    @Override
    public void delete(Project project) {
        projectRepository.delete(project);
    }
}
