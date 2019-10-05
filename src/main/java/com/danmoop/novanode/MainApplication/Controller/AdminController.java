package com.danmoop.novanode.MainApplication.Controller;

import com.danmoop.novanode.MainApplication.Model.Project;
import com.danmoop.novanode.MainApplication.Model.User;
import com.danmoop.novanode.MainApplication.Service.ProjectService;
import com.danmoop.novanode.MainApplication.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.UUID;

@Controller
public class AdminController {
    /**
     * @see User, all users have role = 'User' by default, role 'Admin' can be changed only via database editor
     */

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;


    /**
     * This is a really cruel move, this is handled when Ketarn admin wants to ban a sinful soul
     *
     * @param userName is a user's username who is going to be banned
     * @param principal     is an admin user object, who is logged in
     * @return admin page
     */
    @PostMapping("/BanUser")
    public String banUser(@RequestParam("ban_userName") String userName, Principal principal, RedirectAttributes redirectAttributes) {
        User userDB = userService.findByUserName(userName);
        User user = userService.findByUserName(principal.getName());

        if (user.isRoleAdmin() && userDB != null) {
            userDB.setBanned(true);
            userService.save(userDB);
            redirectAttributes.addFlashAttribute("msg", userDB.getUserName() + " has been banned");
        }

        return "redirect:/admin";
    }


    /**
     * This is handled when admin wants to Unban a user
     *
     * @param userName is a user's username who is going to be Unbanned
     * @param principal     is an admin user object, who is logged in
     * @return admin page
     */
    @PostMapping("/UnbanUser")
    public String unbanUser(@RequestParam("unban_userName") String userName, Principal principal, RedirectAttributes redirectAttributes) {
        User userDB = userService.findByUserName(userName);
        User user = userService.findByUserName(principal.getName());

        if (user.isRoleAdmin() && userDB != null) {
            userDB.setBanned(false);
            userService.save(userDB);
            redirectAttributes.addFlashAttribute("msg", userDB.getUserName() + " has been unbanned");
        }

        return "redirect:/admin";
    }


    /**
     * This is handled when admin wants to know everything about user, it will show JSON object
     *
     * @param username   is user's username
     * @param principal  is an admin, who is logged in
     * @return some user's data
     */
    @PostMapping("/getUserInfo")
    public String userInfo(Principal principal, @RequestParam("username") String username, RedirectAttributes redirectAttributes) {
        User userDB = userService.findByUserName(username);
        User user = userService.findByUserName(principal.getName());

        if (user.isRoleAdmin() && userDB != null)
            redirectAttributes.addFlashAttribute("userInfo", userDB.toString());
        else
            redirectAttributes.addFlashAttribute("userInfo", username + " is not registered");

        return "redirect:/admin";
    }


    /**
     * This is handled when admin wants to know everything about some project, it will show JSON object
     *
     * @param projectName   is project's name
     * @param principal     is an admin, who is logged in
     * @return some project's data
     */
    @PostMapping("/getProjectInfo")
    public String projectInfo(Principal principal, @RequestParam("projectName") String projectName, RedirectAttributes redirectAttributes) {
        Project project = projectService.findByName(projectName);
        User user = userService.findByUserName(principal.getName());

        if (user.isRoleAdmin() && project != null)
            redirectAttributes.addFlashAttribute("projectInfo", project.toString());
        else
            redirectAttributes.addFlashAttribute("projectInfo", projectName + " is not found");

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