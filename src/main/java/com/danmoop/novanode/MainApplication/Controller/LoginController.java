package com.danmoop.novanode.MainApplication.Controller;

import com.danmoop.novanode.MainApplication.Model.User;
import com.danmoop.novanode.MainApplication.Service.Encrypt;
import com.danmoop.novanode.MainApplication.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

@Controller
@SessionAttributes(value = "LoggedUser")
public class LoginController
{
    @Autowired
    private UserService userService;

    /**
     * This request handles authorization
     * if username and password (compared to MD5 database version) are valid -> log in
     *
     * @param userName is a user's username
     * @param password is a user's password
     *
     * @return dashboard page if authorized
     */
    @PostMapping("/loginAttempt")
    public String loginAttempt(Model model, RedirectAttributes redirectAttributes, @RequestParam("userName") String userName, @RequestParam("password") String password) throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        User userInDB = userService.findByUserName(userName);

        if(userInDB != null)
        {
            if(Encrypt.toMD5(password).equals(userInDB.getPassword()))
            {
                model.addAttribute("LoggedUser", userInDB);

                redirectAttributes.addFlashAttribute("welcomeMsg", "Hello, " + userInDB.getName().split(" ")[0] + "!");

                return "redirect:/dashboard";
            }
            else
            {
                redirectAttributes.addFlashAttribute("errorMsg", "Username or password is wrong!");
                return "redirect:/signin";
            }
        }

        else
        {
            redirectAttributes.addFlashAttribute("errorMsg", "Username or password is wrong!");
            return "redirect:/signin";
        }
    }


    /**
     * This request handles removes user's session
     *
     * @return index page
     */
    @GetMapping("/logout")
    public String logout(SessionStatus sessionStatus)
    {
        sessionStatus.setComplete();

        return "redirect:/";
    }
}
