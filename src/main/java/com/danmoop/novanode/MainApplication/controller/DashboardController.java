package com.danmoop.novanode.MainApplication.controller;

import com.danmoop.novanode.MainApplication.model.User;
import com.danmoop.novanode.MainApplication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class DashboardController {

    @Autowired
    private UserRepository userRepository;

    /**
     * This request is handled when user wants to read all inbox and move all messages to 'Read' folder
     *
     * @param auth is a logged-in user object
     * @return dashboard page
     */
    @PostMapping("/readAllInbox")
    public String readAllInbox(Principal auth, RedirectAttributes redirectAttributes) {
        if (auth == null) {
            return "redirect:/";
        }

        User user = userRepository.findByUserName(auth.getName()); // find user in database by username

        if (user.isBanned()) {
            return userIsBanned();
        }

        user.readAllInboxMessages(); // empty messages array
        userRepository.save(user); // save user to database

        redirectAttributes.addFlashAttribute("successMsg", "All inbox marked as read!"); // notify user on their page

        return "redirect:/dashboard";
    }

    /**
     * This request is handled when user wants to delete all 'Read' messages
     *
     * @param auth is a logged-in user object
     * @return dashboard page
     */
    @PostMapping("/deleteAllInbox")
    public String deleteAllInbox(Principal auth, RedirectAttributes redirectAttributes) {
        if (auth == null) {
            return "redirect:/";
        }

        User user = userRepository.findByUserName(auth.getName());

        if (user.isBanned()) {
            return userIsBanned();
        }

        user.getReadMessages().clear();
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMsg", "All inbox deleted!");

        return "redirect:/dashboard";
    }

    /**
     * This request is handled when user wants to edit dashboard notes
     *
     * @param auth     is a logged-in user object
     * @param noteText is a note text
     * @return dashboard page
     */
    @PostMapping("/editUserNotes")
    public String editUserNotes(@RequestParam String noteText, Principal auth) {
        if (auth == null) {
            return "redirect:/";
        }

        User user = userRepository.findByUserName(auth.getName());

        if (user.isBanned()) {
            return userIsBanned();
        }

        user.setNote(noteText);
        userRepository.save(user);

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