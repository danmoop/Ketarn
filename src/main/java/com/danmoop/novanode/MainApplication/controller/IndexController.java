package com.danmoop.novanode.MainApplication.controller;

import com.danmoop.novanode.MainApplication.model.User;
import com.danmoop.novanode.MainApplication.repository.ProjectRepository;
import com.danmoop.novanode.MainApplication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class IndexController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    /**
     * This request displays index page if user is not banned
     *
     * @param auth is a logged-in user object
     * @return index page
     */
    @GetMapping("/")
    public String indexPage(Principal auth) {
        if (auth == null) {
            return "sections/index";
        } else {
            User user = userRepository.findByUserName(auth.getName());

            if (!user.isBanned()) {
                return "redirect:/dashboard";
            } else {
                SecurityContextHolder.clearContext(); // log out user
                return "handlingPages/youarebanned";
            }
        }
    }

    /**
     * This request displays register page with all the text fields
     *
     * @return register page
     */
    @GetMapping("/register")
    public String registerPage() {
        return "sections/registerPage";
    }

    /**
     * This request displays user's dashboard if user is not banned
     *
     * @param auth is a logged-in user object
     * @return dashboard page
     */
    @GetMapping("/dashboard")
    public String dashBoardPage(Principal auth, Model model) {
        if (auth == null) {
            return "redirect:/";
        }

        User user = userRepository.findByUserName(auth.getName());

        if (!user.isBanned()) {
            model.addAttribute("LoggedUser", user);

            model.addAttribute("workSuccess", user.getWorkSuccessAverage());

            return "sections/dashboard";
        } else {
            SecurityContextHolder.clearContext();
            return "handlingPages/youarebanned";
        }
    }
}