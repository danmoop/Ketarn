package com.danmoop.novanode.MainApplication.controller;

import com.danmoop.novanode.MainApplication.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    /**
     * This request displays sign in page if not authorized
     *
     * @return required page
     */
    @GetMapping("/signin")
    public String signInPage() {
        return "redirect:/dashboard";
    }
}