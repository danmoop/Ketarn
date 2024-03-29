package com.danmoop.novanode.MainApplication.controller;

import com.danmoop.novanode.MainApplication.model.Project;
import com.danmoop.novanode.MainApplication.model.User;
import com.danmoop.novanode.MainApplication.repository.ProjectRepository;
import com.danmoop.novanode.MainApplication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
public class AdminController {

    /**
     * @see User, all users have role = 'User' by default, role 'Admin' can be changed only via database editor
     */

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    /**
     * This request displays admin page where you can control the world of Ketarn
     *
     * @param auth is a logged-in user object
     * @return admin page if user is really an admin
     */
    @GetMapping("/admin")
    public String adminPage(Principal auth, Model model) {
        User user = userRepository.findByUserName(auth.getName());

        if (user != null) {
            if (user.getRole().equals("Admin")) {
                List<User> users = userRepository.findAll();
                List<Project> projects = projectRepository.findAll();

                model.addAttribute("usersLength", users.size());
                model.addAttribute("projectsLength", projects.size());
                model.addAttribute("LoggedUser", user);

                return "admin/adminPage";
            }
        }

        return "redirect:/";
    }

    /**
     * This is a really cruel move, this is handled when Ketarn admin wants to ban a sinful soul
     *
     * @param userName is a user's username who is going to be banned
     * @param auth     is an admin user object, who is logged in
     * @return admin page
     */
    @PostMapping("/BanUser")
    public String banUser(@RequestParam("ban_userName") String userName, Principal auth, RedirectAttributes redirectAttributes) {
        User userDB = userRepository.findByUserName(userName);
        User user = userRepository.findByUserName(auth.getName());

        if (user.isRoleAdmin() && userDB != null) {
            userDB.setBanned(true);
            userRepository.save(userDB);
            redirectAttributes.addFlashAttribute("msg", userDB.getUserName() + " has been banned");
        } else {
            redirectAttributes.addFlashAttribute("err_ban", userName + " is not registered");
        }

        return "redirect:/admin";
    }


    /**
     * This is handled when admin wants to Unban a user
     *
     * @param userName is a user's username who is going to be Unbanned
     * @param auth     is an admin user object, who is logged in
     * @return admin page
     */
    @PostMapping("/UnbanUser")
    public String unbanUser(@RequestParam("unban_userName") String userName, Principal auth, RedirectAttributes redirectAttributes) {
        User userDB = userRepository.findByUserName(userName);
        User user = userRepository.findByUserName(auth.getName());

        if (user.isRoleAdmin() && userDB != null) {
            userDB.setBanned(false);
            userRepository.save(userDB);
            redirectAttributes.addFlashAttribute("msg", userDB.getUserName() + " has been unbanned");
        } else {
            redirectAttributes.addFlashAttribute("err_unban", userName + " is not registered");
        }

        return "redirect:/admin";
    }


    /**
     * This is handled when admin wants to know everything about user, it will show JSON object
     *
     * @param username is user's username
     * @param auth     is an admin, who is logged in
     * @return some user's data
     */
    @PostMapping("/getUserInfo")
    public String userInfo(Principal auth, @RequestParam String username, RedirectAttributes redirectAttributes) {
        User userDB = userRepository.findByUserName(username);
        User user = userRepository.findByUserName(auth.getName());

        if (user.isRoleAdmin() && userDB != null) {
            redirectAttributes.addFlashAttribute("userInfo", userDB.toString());
        } else {
            redirectAttributes.addFlashAttribute("userInfo", username + " is not registered");
        }

        return "redirect:/admin";
    }

    /**
     * This is handled when admin wants to know everything about some project, it will show JSON object
     *
     * @param projectName is project's name
     * @param auth        is an admin, who is logged in
     * @return some project's data
     */
    @PostMapping("/getProjectInfo")
    public String projectInfo(Principal auth, @RequestParam String projectName, RedirectAttributes redirectAttributes) {
        Project project = projectRepository.findByName(projectName);
        User user = userRepository.findByUserName(auth.getName());

        if (user.isRoleAdmin() && project != null) {
            redirectAttributes.addFlashAttribute("projectInfo", project.toString());
        } else {
            redirectAttributes.addFlashAttribute("projectInfo", projectName + " is not found");
        }

        return "redirect:/admin";
    }

    /**
     * @return random UUID
     */
    @PostMapping("/giveuuid")
    public String giveuuid(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("uuid", "UUID: " + UUID.randomUUID());
        return "redirect:/admin";
    }
}