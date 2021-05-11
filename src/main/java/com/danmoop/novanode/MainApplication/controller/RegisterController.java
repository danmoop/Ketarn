package com.danmoop.novanode.MainApplication.controller;

import com.danmoop.novanode.MainApplication.model.User;
import com.danmoop.novanode.MainApplication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegisterController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * This request is handled when user wants to register
     * There is BCrypt implemented for encrypting users' passwords
     * If there is not user with identical username - register, otherwise show an error message
     *
     * @param name     is taken from a html text field
     * @param userName is taken from a html text field
     * @param email    is taken from a html text field
     * @param password is taken from a html text field
     * @return index page if registration is successful, otherwise return register page again and show an error message
     */
    @PostMapping("/register")
    public String registerSubmit(@RequestParam("name") String name, @RequestParam("userName") String userName, @RequestParam("email") String email, @RequestParam("password") String password, RedirectAttributes redirectAttributes) {
        if (userRepository.findByUserName(userName) == null) {
            User user = new User(userName, name, email, passwordEncoder.encode(password));
            userRepository.save(user);

            redirectAttributes.addFlashAttribute("successMsg", "Registered successfully! Now you can sign in using your login and password!");

            return "redirect:/";
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "This username is already taken!");
            return "redirect:/register";
        }
    }
}