package com.danmoop.novanode.MainApplication.Controller;

import com.danmoop.novanode.MainApplication.Model.User;
import com.danmoop.novanode.MainApplication.Service.Encrypt;
import com.danmoop.novanode.MainApplication.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

@Controller
@SessionAttributes(value = "LoggedUser")
public class DashboardController
{
    @Autowired
    private UserService userService;


    /**
     * This request is handled when user wants to read all inbox and move all messages to 'Read' folder

     * @param user is a logged-in user object
     *
     * @return dashboard page
     */
    @PostMapping("/readAllInbox")
    public String readAllInbox(@ModelAttribute("LoggedUser") User user, RedirectAttributes redirectAttributes)
    {
        User userDB = userService.findByUserName(user.getUserName()); // find user in database by username

        userDB.readAllInboxMessages(); // empty messages array

        userService.save(userDB); // save user to database

        redirectAttributes.addFlashAttribute("successMsg", "All inbox marked as read!"); // notify user on their page

        return "redirect:/dashboard";
    }


    /**
     * This request is handled when user wants to delete all 'Read' messages
     *
     * @param user is a logged-in user object
     *
     * @return dashboard page
     */
    @PostMapping("/deleteAllInbox")
    public String deleteAllInbox(@ModelAttribute("LoggedUser") User user, RedirectAttributes redirectAttributes)
    {
        User userDB = userService.findByUserName(user.getUserName());

        userDB.emptyArchive();

        userService.save(userDB);

        redirectAttributes.addFlashAttribute("successMsg", "All inbox deleted!");

        return "redirect:/dashboard";
    }


    /**
     * This request is handled when user wants to change password
     *
     * @param user is a logged-in user object
     * @param oldPass is an old password, taken from html input field
     * @param newPass is a new password, taken from html input field
     * @param confirmPass is a new password, taken from html input field
     *
     * @return dashboard page
     */
    @PostMapping("/changePassword")
    public String changePassword(
            @ModelAttribute("LoggedUser") User user,
            @RequestParam("old_pass") String oldPass,
            @RequestParam("new_pass") String newPass,
            @RequestParam("confirm_pass") String confirmPass,
            RedirectAttributes redirectAttributes) throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        User userDB = userService.findByUserName(user.getUserName());

        /*
            We compare an old password to a current password, so with new pass and confirmation
            If everything is fine, we proceed and rewrite user's information
        */
        if(Encrypt.toMD5(oldPass).equals(userDB.getPassword()) && newPass.equals(confirmPass))
        {
            userDB.setPassword(Encrypt.toMD5(newPass));
            userService.save(userDB);

            redirectAttributes.addFlashAttribute("successMsg", "Password changed successfully!");

            return "redirect:/dashboard";
        }

        else
        {
            redirectAttributes.addFlashAttribute("errorMsg", "Unable to change password, passwords don't match");
            return "redirect:/dashboard";
        }

    }


    /**
     * This request is handled when user wants to edit dashboard notes
     *
     * @param user is a logged-in user object
     * @param noteText is a note text
     *
     * @return dashboard page
     */
    @PostMapping("/editUserNotes")
    public String editUserNotes(@RequestParam("noteText") String noteText, @ModelAttribute("LoggedUser") User user)
    {
        User userDB = userService.findByUserName(user.getUserName());

        userDB.setNote(noteText);
        userService.save(userDB);

        return "redirect:/dashboard";
    }
}