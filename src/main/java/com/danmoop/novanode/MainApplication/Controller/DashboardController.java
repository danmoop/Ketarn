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
    UserService userService;

    @PostMapping("/readAllInbox")
    public String readAllInbox(@ModelAttribute("LoggedUser") User user, RedirectAttributes redirectAttributes)
    {
        User userDB = userService.findByUserName(user.getUserName());

        userDB.readAllInboxMessages();

        userService.save(userDB);

        redirectAttributes.addFlashAttribute("successMsg", "All inbox marked as read!");

        return "redirect:/dashboard";
    }

    @PostMapping("/deleteAllInbox")
    public String deleteAllInbox(@ModelAttribute("LoggedUser") User user, RedirectAttributes redirectAttributes)
    {
        User userDB = userService.findByUserName(user.getUserName());

        userDB.emptyArchive();

        userService.save(userDB);

        redirectAttributes.addFlashAttribute("successMsg", "All inbox deleted!");

        return "redirect:/dashboard";
    }

    @PostMapping("/changePassword")
    public String changePassword(
            @ModelAttribute("LoggedUser") User user,
            @RequestParam("old_pass") String oldPass,
            @RequestParam("new_pass") String newPass,
            @RequestParam("confirm_pass") String confirmPass,
            RedirectAttributes redirectAttributes) throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        User userDB = userService.findByUserName(user.getUserName());

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
}