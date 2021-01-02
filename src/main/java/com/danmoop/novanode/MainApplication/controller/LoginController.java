package com.danmoop.novanode.MainApplication.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

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