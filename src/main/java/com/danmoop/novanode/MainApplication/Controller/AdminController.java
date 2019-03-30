package com.danmoop.novanode.MainApplication.Controller;

import com.danmoop.novanode.MainApplication.Model.Project;
import com.danmoop.novanode.MainApplication.Model.User;
import com.danmoop.novanode.MainApplication.Service.ProjectService;
import com.danmoop.novanode.MainApplication.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@SessionAttributes(value = "LoggedUser")
public class AdminController
{
    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @PostMapping("/BanUser")
    public String banUser(@RequestParam("ban_userName") String userName, @ModelAttribute("LoggedUser") User user, RedirectAttributes redirectAttributes)
    {
        User userDB = userService.findByUserName(userName);

        if(user.isRoleAdmin() && userDB != null)
        {
            userDB.setBanned(true);
            userService.save(userDB);
            redirectAttributes.addFlashAttribute("msg", userDB.getUserName() + " has been banned");
        }

        return "redirect:/admin";
    }

    @PostMapping("/UnbanUser")
    public String unbanUser(@RequestParam("unban_userName") String userName, @ModelAttribute("LoggedUser") User user, RedirectAttributes redirectAttributes)
    {
        User userDB = userService.findByUserName(userName);

        if(user.isRoleAdmin() && userDB != null)
        {
            userDB.setBanned(false);
            userService.save(userDB);
            redirectAttributes.addFlashAttribute("msg", userDB.getUserName() + " has been unbanned");
        }

        return "redirect:/admin";
    }

    @PostMapping("/getUserInfo")
    public String userInfo(@ModelAttribute("LoggedUser") User user, @RequestParam("username") String username, RedirectAttributes redirectAttributes)
    {
        User userDB = userService.findByUserName(username);

        if(user.isRoleAdmin() && userDB != null)
            redirectAttributes.addFlashAttribute("userInfo", userDB.toString());
        else
            redirectAttributes.addFlashAttribute("userInfo", username + " is not registered");

        return "redirect:/admin";
    }

    @PostMapping("/getProjectInfo")
    public String projectInfo(@ModelAttribute("LoggedUser") User user, @RequestParam("projectName") String projectName, RedirectAttributes redirectAttributes)
    {
        Project project = projectService.findByName(projectName);

        if(user.isRoleAdmin() && project != null)
            redirectAttributes.addFlashAttribute("projectInfo", project.toString());
        else
            redirectAttributes.addFlashAttribute("projectInfo", projectName + " is not found");

        return "redirect:/admin";
    }

    @PostMapping("/giveuuid")
    public String giveuuid(RedirectAttributes redirectAttributes)
    {
        redirectAttributes.addFlashAttribute("uuid", "UUID: " + UUID.randomUUID());
        return "redirect:/admin";
    }
}