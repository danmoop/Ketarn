package com.danmoop.novanode.MainApplication.Controller;

import com.danmoop.novanode.MainApplication.Model.User;
import com.danmoop.novanode.MainApplication.Service.Encrypt;
import com.danmoop.novanode.MainApplication.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

@Controller
@SessionAttributes(value = "LoggedUser")
public class RegisterController
{
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public String registerSubmit(
            @RequestParam("name") String name,
            @RequestParam("userName") String userName,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            Model model,
            RedirectAttributes redirectAttributes
    ) throws UnsupportedEncodingException, NoSuchAlgorithmException

    {
        if(userService.findByUserName(userName) == null)
        {

            User newUser = new User(userName, name, email, Encrypt.toMD5(password));

            userService.save(newUser);

            redirectAttributes.addFlashAttribute("successMsg", "Registered successfully! Now you can sign in using your login and password!");

            return "redirect:/";
        }
        else
        {
            redirectAttributes.addFlashAttribute("errorMsg", "This username is already taken!");
            return "redirect:/register";
        }
    }

    @GetMapping("/reg_error")
    public String userExists(Model model)
    {
        model.addAttribute("errorMessage", "Such user already exists");
        return "sections/registerPage";
    }
}
