package com.danmoop.novanode.MainApplication.controller;

import com.danmoop.novanode.MainApplication.model.Project;
import com.danmoop.novanode.MainApplication.model.User;
import com.danmoop.novanode.MainApplication.repository.ProjectRepository;
import com.danmoop.novanode.MainApplication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class APIController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @GetMapping("/gen/{pass}")
    public String gen(@PathVariable String pass) {
        return encoder.encode(pass);
    }

    /**
     * This request displays project JSON information
     *
     * @param projectName is a project name, taken from an address bar
     * @param principal   is a logged-in user object
     * @return project JSON, if credentials are invalid - return empty project
     */
    @GetMapping("/getProjectJson/{projectName}")
    public Project getProjectJson(@PathVariable("projectName") String projectName, Principal principal) {
        Project project = projectRepository.findByName(projectName);
        User user = userRepository.findByUserName(principal.getName());

        if (project.getAuthorName().equals(user.getUserName())) {
            return project;
        }

        return new Project();
    }

    /**
     * This request displays user JSON information
     *
     * @param userName  is taken from an address bar
     * @param principal is a logged-in user object
     * @return user JSON, if credentials are invalid - return empty user
     */
    @GetMapping("/getUserJson/{userName}")
    public User getUserJson(@PathVariable("userName") String userName, Principal principal) {
        if (principal.getName().equals(userName)) {
            return userRepository.findByUserName(userName);
        }

        return new User();
    }

    /**
     * If user is logged in -> display principal data
     *
     * @param principal is a logged-in user object
     * @return principal
     */
    @GetMapping("/principal")
    public Principal principal(Principal principal) {
        return principal != null ? principal : () -> "Unauthorized";
    }
}