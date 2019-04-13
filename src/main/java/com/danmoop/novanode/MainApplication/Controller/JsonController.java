package com.danmoop.novanode.MainApplication.Controller;


import com.danmoop.novanode.MainApplication.Model.Project;
import com.danmoop.novanode.MainApplication.Model.User;
import com.danmoop.novanode.MainApplication.Service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@SessionAttributes(value = "LoggedUser")
public class JsonController
{
    @Autowired
    private ProjectService projectService;

    @GetMapping("/getProjectJson/{projectName}")
    public String json(@PathVariable("projectName") String projectName, @ModelAttribute("LoggedUser") User user)
    {
        Project project = projectService.findByName(projectName);

        if(project.getAuthorName().equals(user.getUserName()))
            return project.toString();

        return "Information is not available";
    }
}