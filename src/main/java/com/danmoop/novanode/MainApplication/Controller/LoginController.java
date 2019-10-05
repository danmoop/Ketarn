package com.danmoop.novanode.MainApplication.Controller;

import com.danmoop.novanode.MainApplication.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.security.Principal;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    /**
     * This request displays sign in page if not authorized
     *
     * @param user is a logged-in user object
     * @return required page
     */
    @GetMapping("/signin")
    public String signInPage(Model model, Principal user) {
        return "redirect:/dashboard";
    }

    @GetMapping("/log-out")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        return "redirect:/";
    }
}
