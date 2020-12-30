package com.danmoop.novanode.MainApplication.controller;

import com.danmoop.novanode.MainApplication.model.InboxMessage;
import com.danmoop.novanode.MainApplication.model.Project;
import com.danmoop.novanode.MainApplication.model.User;
import com.danmoop.novanode.MainApplication.repository.ProjectService;
import com.danmoop.novanode.MainApplication.repository.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;

/**
 * @class UserController
 * This class mostly manipulates with project tasks: completion, sending requests etc.
 */
@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * This request is handled when user wants to change their info - username and email
     *
     * @param principal          is a logged-in user object
     * @param name               is taken from html textfield
     * @param email              is taken from html textfield
     * @param redirectAttributes is assigned automatically, it is used to display a message after redirect
     * @return dashboard page with a message added to @param redirectAttributes
     */
    @PostMapping("/editProfileInfo")
    public String editProfileInfo(
            Principal principal,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes) {

        User userDB = userService.findByUserName(principal.getName());

        userDB.setName(name);
        userDB.setEmail(email);
        userService.save(userDB);

        redirectAttributes.addFlashAttribute("successMsg", "Info changed successfully!");

        return "redirect:/dashboard";
    }

    /**
     * This request is handled when user wants to change password
     *
     * @param principal   is a logged-in user object
     * @param oldPass     is an old password, taken from html input field
     * @param newPass     is a new password, taken from html input field
     * @param confirmPass is a new password, taken from html input field
     * @return dashboard page
     */
    @PostMapping("/changePassword")
    public String changePassword(
            Principal principal,
            @RequestParam("old_pass") String oldPass,
            @RequestParam("new_pass") String newPass,
            @RequestParam("confirm_pass") String confirmPass,
            RedirectAttributes redirectAttributes) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        User userDB = userService.findByUserName(principal.getName());

        /*
            We compare an old password to a current password, so with new pass and confirmation
            If everything is fine, we proceed and rewrite user's information
        */
        if (passwordEncoder.encode(oldPass).equals(userDB.getPassword()) && newPass.equals(confirmPass)) {
            userDB.setPassword(passwordEncoder.encode(newPass));
            userService.save(userDB);

            redirectAttributes.addFlashAttribute("successMsg", "Password changed successfully!");

            return "redirect:/dashboard";
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "Unable to change password, passwords don't match");
            return "redirect:/dashboard";
        }

    }

    /**
     * This request is handled when project's admin accepts the request sent by a user to join their project
     *
     * @param principal   is a logged-in user object
     * @param authorName  is taken from html textfield
     * @param projectName is taken from html textfield
     * @param messageKey  is taken from a hidden html textfield. Value is assigned using Thymeleaf
     * @return dashboard page. Add member to project, add message about new member, save project and member objects
     */
    @PostMapping("/acceptRequest")
    public String requestAccepted(
            Principal principal,
            @RequestParam("authorName") String authorName,
            @RequestParam("projectName") String projectName,
            @RequestParam("messageKey") String messageKey,
            RedirectAttributes redirectAttributes) {

        User userDB = userService.findByUserName(principal.getName());
        User authorDB = userService.findByUserName(authorName);
        Project projectDB = projectService.findByName(projectName);

        if (userDB.isProjectAdmin(projectDB)) {
            projectDB.addMember(authorName);

            projectDB.addMessage(new InboxMessage(principal.getName() + " has accepted a new member - " + authorName, principal.getName(), "inboxMessage"));

            authorDB.addProjectTakingPartIn(projectName);
            InboxMessage message = new InboxMessage(principal.getName() + " has accepted your request in " + projectName + " project!", principal.getName(), "inboxMessage");
            authorDB.addMessage(message);
            userDB.removeMessageFromInbox(userDB.findMessageByMessageKey(messageKey));

            userService.save(authorDB);
            userService.save(userDB);
            projectService.save(projectDB);

            redirectAttributes.addFlashAttribute("successMsg", authorName + "'s request accepted!");
        }

        return "redirect:/dashboard";
    }


    /**
     * This request is handled when project's admin rejected the join request sent by a user
     *
     * @param principal   is a logged-in user object
     * @param authorName  is taken from html textfield
     * @param projectName is taken from html textfield
     * @param messageKey  is taken from a hidden html textfield. Value is assigned using Thymeleaf
     * @return dashboard page. Send a rejection message and save user object
     */
    @PostMapping("/rejectRequest")
    public String requestRejected(
            Principal principal,
            @RequestParam("authorName") String authorName,
            @RequestParam("projectName") String projectName,
            @RequestParam("messageKey") String messageKey,
            RedirectAttributes redirectAttributes) {

        User userDB = userService.findByUserName(principal.getName());
        User authorDB = userService.findByUserName(authorName);

        InboxMessage message = new InboxMessage(principal.getName() + " has rejected your request in " + projectName + " project.", principal.getName(), "inboxMessage");

        authorDB.addMessage(message);

        userDB.removeMessageFromInbox(userDB.findMessageByMessageKey(messageKey));
        userService.save(authorDB);
        userService.save(userDB);

        redirectAttributes.addFlashAttribute("errorMsg", authorName + "'s request rejected");

        return "redirect:/dashboard";
    }


    /**
     * This request is handled when user accepts an invitation to a project sent before by project's admin
     *
     * @param principal   is a logged-in user object
     * @param authorName  is taken from html textfield
     * @param projectName is taken from html textfield
     * @param messageKey  is taken from a hidden html textfield. Value is assigned using Thymeleaf
     * @return dashboard page. Add member to project, add message about new member, save project and member objects
     */
    @PostMapping("/acceptProjectInvite")
    public String acceptProjectInvite(
            Principal principal,
            @RequestParam("authorName") String authorName,
            @RequestParam("projectName") String projectName,
            @RequestParam("messageKey") String messageKey,
            RedirectAttributes redirectAttributes) {

        User userDB = userService.findByUserName(principal.getName());
        User authorDB = userService.findByUserName(authorName);
        Project projectDB = projectService.findByName(projectName);

        InboxMessage message = new InboxMessage(principal.getName() + " has accepted your request in " + projectName + " project.", principal.getName(), "inboxMessage");
        authorDB.addMessage(message);

        userDB.removeMessageFromInbox(userDB.findMessageByMessageKey(messageKey));

        projectDB.addMember(principal.getName());
        projectDB.addMessage(new InboxMessage(authorName + " has accepted " + principal.getName() + " to the project!", authorName, "inboxMessage"));

        userDB.addProjectTakingPartIn(projectName);

        userService.save(authorDB);
        userService.save(userDB);
        projectService.save(projectDB);

        redirectAttributes.addFlashAttribute("successMsg", authorName + "'s invite accepted");

        return "redirect:/dashboard";
    }


    /**
     * This request is handled when user rejects an invitation to a project sent before by project's admin
     *
     * @param principal   is a logged-in user object
     * @param authorName  is taken from html textfield
     * @param projectName is taken from html textfield
     * @param messageKey  is taken from a hidden html textfield. Value is assigned using Thymeleaf
     * @return dashboard page and notify users about rejection
     */
    @PostMapping("/rejectProjectInvite")
    public String rejectProjectInvite(
            Principal principal,
            @RequestParam("authorName") String authorName,
            @RequestParam("projectName") String projectName,
            @RequestParam("messageKey") String messageKey,
            RedirectAttributes redirectAttributes) {

        User userDB = userService.findByUserName(principal.getName());
        User authorDB = userService.findByUserName(authorName);

        InboxMessage message = new InboxMessage(principal.getName() + " has rejected your request in " + projectName + " project.", principal.getName(), "inboxMessage");
        authorDB.addMessage(message);
        userDB.removeMessageFromInbox(userDB.findMessageByMessageKey(messageKey));

        userService.save(authorDB);
        userService.save(userDB);

        redirectAttributes.addFlashAttribute("errorMsg", authorName + "'s invite rejected");

        return "redirect:/dashboard";
    }
}