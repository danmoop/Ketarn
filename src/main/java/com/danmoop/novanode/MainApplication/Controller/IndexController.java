package com.danmoop.novanode.MainApplication.Controller;

import com.danmoop.novanode.MainApplication.Model.Project;
import com.danmoop.novanode.MainApplication.Model.User;
import com.danmoop.novanode.MainApplication.Service.ProjectService;
import com.danmoop.novanode.MainApplication.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import java.util.List;

@Controller
@SessionAttributes(value = "LoggedUser")
public class IndexController
{
    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;


    /**
     * @return LoggedUser which is null by default in order to prevent Spring from displaying error
     */
    @ModelAttribute(value = "LoggedUser")
    public User nullUser() {
        return null;
    }


    /**
     * This request displays index page if user is not banned
     *
     * @param user is a logged-in user object
     * @param status is a session status, assigned automatically by Spring
     *
     * @return index page
     */
    @GetMapping("/")
    public String indexPage(@ModelAttribute(value = "LoggedUser") User user, SessionStatus status)
    {
        if(user == null)
        {
            return "sections/index";
        }
        else
        {
            if(!user.isBanned())
            {
                return "redirect:/dashboard";
            }
            else
            {
                status.setComplete(); // log out user
                return "handlingPages/youarebanned";
            }
        }
    }

    /**
     * This request displays register page with all the textfields
     *
     * @return register page
     */
    @GetMapping("/register")
    public String registerPage()
    {
        return "sections/registerPage";
    }


    /**
     * This request displays user's dashboard if user is not banned
     *
     * @param user is a logged-in user object
     *
     * @return dashboard page
     */
    @GetMapping("/dashboard")
    public String dashBoardPage(@ModelAttribute(value = "LoggedUser") User user, Model model, SessionStatus status)
    {
        if(user == null)
        {
            return "redirect:/";
        }
        else
        {
            if(!user.isBanned())
            {
                model.addAttribute("LoggedUser", userService.findByUserName(user.getUserName()));

                double ws = userService.findByUserName(user.getUserName()).getWorkSuccessAverage();

                model.addAttribute("workSuccess", ws);

                return "sections/dashboard";
            }
            else
            {
                status.setComplete();
                return "handlingPages/youarebanned";
            }
        }
    }


    /**
     * This request displays sign in page if not authorized
     *
     * @param user is a logged-in user object
     *
     * @return required page
     */
    @GetMapping("/signin")
    public String signInPage(Model model, @ModelAttribute(value = "LoggedUser") User user)
    {

        if(user == null)
            return "sections/signInPage";
        else
            return "redirect:/dashboard";
    }


    /**
     * This request displays admin page where you can control the world of Ketarn
     *
     * @param user is a logged-in user object
     *
     * @return admin page if user is really an admin
     */
    @GetMapping("/admin")
    public String adminPage(@ModelAttribute("LoggedUser") User user, Model model)
    {
        if(user != null)
        {
            if(userService.findByUserName(user.getUserName()).getRole().equals("Admin"))
            {
                List<User> users = userService.findAll();
                List<Project> projects = projectService.findAll();

                model.addAttribute("usersLength", users.size());
                model.addAttribute("projectsLength", projects.size());

                return "admin/adminPage";
            }
        }

        return "redirect:/";
    }
}