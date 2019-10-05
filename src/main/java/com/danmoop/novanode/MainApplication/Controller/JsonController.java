package com.danmoop.novanode.MainApplication.Controller;

import com.danmoop.novanode.MainApplication.Model.Project;
import com.danmoop.novanode.MainApplication.Model.User;
import com.danmoop.novanode.MainApplication.Service.ProjectService;
import com.danmoop.novanode.MainApplication.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@SessionAttributes(value = "LoggedUser")
public class JsonController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    /**
     * This request displays project JSON object
     *
     * @param projectName is a project name
     * @param principal   is a logged-in user object
     * @return project JSON
     */
    @GetMapping("/getProjectJson/{projectName}")
    public String json(@PathVariable("projectName") String projectName, Principal principal) {
        Project project = projectService.findByName(projectName);
        User user = userService.findByUserName(principal.getName());

        if (project.getAuthorName().equals(user.getUserName()))
            return project.toString();

        return "Information is not available";
    }

    @GetMapping("/principal")
    public Principal principal(Principal principal) {
        return principal != null ? principal : () -> "Unauthorized";
    }
}