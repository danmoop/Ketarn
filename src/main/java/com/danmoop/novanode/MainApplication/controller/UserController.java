package com.danmoop.novanode.MainApplication.controller;

import com.danmoop.novanode.MainApplication.model.InboxMessage;
import com.danmoop.novanode.MainApplication.model.Project;
import com.danmoop.novanode.MainApplication.model.User;
import com.danmoop.novanode.MainApplication.repository.ProjectRepository;
import com.danmoop.novanode.MainApplication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/**
 * @class UserController
 * This class mostly manipulates with project tasks: completion, sending requests etc.
 */
@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * This request is handled when user wants to change their info - username and email
     *
     * @param auth               is a logged-in user object
     * @param name               is taken from html text field
     * @param email              is taken from html text field
     * @param redirectAttributes is assigned automatically, it is used to display a message after redirect
     * @return dashboard page with a message added to @param redirectAttributes
     */
    @PostMapping("/editProfileInfo")
    public String editProfileInfo(@RequestParam String name, @RequestParam String email, Principal auth, RedirectAttributes redirectAttributes) {
        if (auth == null) {
            return "redirect:/";
        }

        User user = userRepository.findByUserName(auth.getName());

        if (user.isBanned()) {
            return userIsBanned();
        }

        user.setName(name);
        user.setEmail(email);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMsg", "Info changed successfully!");

        return "redirect:/dashboard";
    }

    /**
     * This request is handled when user wants to change password
     *
     * @param auth        is a logged-in user object
     * @param oldPass     is an old password, taken from html input field
     * @param newPass     is a new password, taken from html input field
     * @param confirmPass is a new password, taken from html input field
     * @return dashboard page
     */
    @PostMapping("/changePassword")
    public String changePassword(@RequestParam("old_pass") String oldPass, @RequestParam("new_pass") String newPass, @RequestParam("confirm_pass") String confirmPass, Principal auth, RedirectAttributes redirectAttributes) {
        if (auth == null) {
            return "redirect:/";
        }

        User user = userRepository.findByUserName(auth.getName());

        if (user.isBanned()) {
            return userIsBanned();
        }

        if (newPass.length() < 8) {
            redirectAttributes.addFlashAttribute("errorMsg", "New password should be at least 8 characters!");
            return "redirect:/dashboard";
        }

        /*
            We compare an old password to a current password, so with new pass and confirmation
            If everything is fine, we proceed and rewrite user's information
        */
        if (passwordEncoder.matches(oldPass, user.getPassword()) && newPass.equals(confirmPass)) {
            user.setPassword(passwordEncoder.encode(newPass));
            userRepository.save(user);

            redirectAttributes.addFlashAttribute("successMsg", "Password changed successfully!");

        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "Unable to change password, passwords don't match");
        }

        return "redirect:/dashboard";
    }

    /**
     * This request is handled when project's admin accepts the request sent by a user to join their project
     *
     * @param auth        is a logged-in user object
     * @param messageKey  is taken from a hidden html text field. Value is assigned using Thymeleaf
     * @param authorName  is taken from html text field
     * @param projectName is taken from html text field
     * @return dashboard page. Add member to project, add message about new member, save project and member objects
     */
    @PostMapping("/acceptRequest")
    public String requestAccepted(@RequestParam String authorName, @RequestParam String projectName, @RequestParam String messageKey, Principal auth, RedirectAttributes redirectAttributes) {
        if (auth == null) {
            return "redirect:/";
        }

        User user = userRepository.findByUserName(auth.getName());

        if (user.isBanned()) {
            return userIsBanned();
        }

        User authorDB = userRepository.findByUserName(authorName);
        Project projectDB = projectRepository.findByName(projectName);

        if (user.isProjectAdmin(projectDB)) {
            projectDB.getMembers().add(authorName);

            projectDB.getProjectInbox().add(new InboxMessage(auth.getName() + " has accepted a new member - " + authorName, auth.getName(), "inboxMessage"));

            authorDB.getProjectsTakePartIn().add(projectName);
            InboxMessage message = new InboxMessage(auth.getName() + " has accepted your request in " + projectName + " project!", auth.getName(), "inboxMessage");
            authorDB.getMessages().add(message);
            user.getMessages().remove(user.findMessageByMessageKey(messageKey));

            userRepository.save(authorDB);
            userRepository.save(user);
            projectRepository.save(projectDB);

            redirectAttributes.addFlashAttribute("successMsg", authorName + "'s request accepted!");
        }

        return "redirect:/dashboard";
    }

    /**
     * This request is handled when project's admin rejected the join request sent by a user
     *
     * @param auth        is a logged-in user object
     * @param authorName  is taken from html text field
     * @param messageKey  is taken from a hidden html text field. Value is assigned using Thymeleaf
     * @param projectName is taken from html text field
     * @return dashboard page. Send a rejection message and save user object
     */
    @PostMapping("/rejectRequest")
    public String requestRejected(@RequestParam String authorName, @RequestParam String projectName, @RequestParam String messageKey, Principal auth, RedirectAttributes redirectAttributes) {
        if (auth == null) {
            return "redirect:/";
        }

        User user = userRepository.findByUserName(auth.getName());

        if (user.isBanned()) {
            return userIsBanned();
        }

        User authorDB = userRepository.findByUserName(authorName);

        InboxMessage message = new InboxMessage(auth.getName() + " has rejected your request in " + projectName + " project.", auth.getName(), "inboxMessage");

        authorDB.getMessages().add(message);

        user.getMessages().remove(user.findMessageByMessageKey(messageKey));
        userRepository.save(authorDB);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("errorMsg", authorName + "'s request rejected");

        return "redirect:/dashboard";
    }

    /**
     * This request is handled when user accepts an invitation to a project sent before by project's admin
     *
     * @param auth        is a logged-in user object
     * @param authorName  is taken from html text field
     * @param projectName is taken from html text field
     * @param messageKey  is taken from a hidden html text field. Value is assigned using Thymeleaf
     * @return dashboard page. Add member to project, add message about new member, save project and member objects
     */
    @PostMapping("/acceptProjectInvite")
    public String acceptProjectInvite(@RequestParam String authorName, @RequestParam String projectName, @RequestParam String messageKey, Principal auth, RedirectAttributes redirectAttributes) {
        if (auth == null) {
            return "redirect:/";
        }

        User user = userRepository.findByUserName(auth.getName());

        if (user.isBanned()) {
            return userIsBanned();
        }

        User authorDB = userRepository.findByUserName(authorName);
        Project projectDB = projectRepository.findByName(projectName);

        InboxMessage message = new InboxMessage(auth.getName() + " has accepted your request in " + projectName + " project.", auth.getName(), "inboxMessage");
        authorDB.getMessages().add(message);

        user.getMessages().remove(user.findMessageByMessageKey(messageKey));

        projectDB.getMembers().add(auth.getName());
        projectDB.getProjectInbox().add(new InboxMessage(authorName + " has accepted " + auth.getName() + " to the project!", authorName, "inboxMessage"));

        user.getProjectsTakePartIn().add(projectName);

        userRepository.save(authorDB);
        userRepository.save(user);
        projectRepository.save(projectDB);

        redirectAttributes.addFlashAttribute("successMsg", authorName + "'s invite accepted");

        return "redirect:/dashboard";
    }

    /**
     * This request is handled when user rejects an invitation to a project sent before by project's admin
     *
     * @param auth        is a logged-in user object
     * @param authorName  is taken from html text field
     * @param projectName is taken from html text field
     * @param messageKey  is taken from a hidden html text field. Value is assigned using Thymeleaf
     * @return dashboard page and notify users about rejection
     */
    @PostMapping("/rejectProjectInvite")
    public String rejectProjectInvite(@RequestParam String authorName, @RequestParam String projectName, @RequestParam String messageKey, Principal auth, RedirectAttributes redirectAttributes) {
        if (auth == null) {
            return "redirect:/";
        }

        User user = userRepository.findByUserName(auth.getName());

        if (user.isBanned()) {
            return userIsBanned();
        }

        User authorDB = userRepository.findByUserName(authorName);

        InboxMessage message = new InboxMessage(auth.getName() + " has rejected your request in " + projectName + " project.", auth.getName(), "inboxMessage");
        authorDB.getMessages().add(message);
        user.getMessages().remove(user.findMessageByMessageKey(messageKey));

        userRepository.save(authorDB);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("errorMsg", authorName + "'s invite rejected");

        return "redirect:/dashboard";
    }

    /**
     * This function has to be called if a user is banned
     *
     * @return a page, which says a user is banned
     */
    private String userIsBanned() {
        SecurityContextHolder.clearContext();
        return "handlingPages/youarebanned";
    }
}