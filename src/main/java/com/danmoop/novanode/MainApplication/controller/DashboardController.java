package com.danmoop.novanode.MainApplication.controller;

import com.danmoop.novanode.MainApplication.model.User;
import com.danmoop.novanode.MainApplication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
        User userDB = userRepository.findByUserName(auth.getName()); // find user in database by username

        userDB.readAllInboxMessages(); // empty messages array
        userRepository.save(userDB); // save user to database

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
        User userDB = userRepository.findByUserName(auth.getName());

        userDB.getReadMessages().clear();
        userRepository.save(userDB);

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
        User userDB = userRepository.findByUserName(auth.getName());

        if (userDB != null) {
            userDB.setNote(noteText);
            userRepository.save(userDB);
        }

        return "redirect:/dashboard";
    }
}