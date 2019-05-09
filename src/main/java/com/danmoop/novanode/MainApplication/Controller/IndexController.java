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

import java.lang.instrument.Instrumentation;
import java.util.List;

@Controller
@SessionAttributes(value = "LoggedUser")
public class IndexController
{
    private static Instrumentation instrumentation;

    public static void premain(String args, Instrumentation inst)
    {
        instrumentation = inst;
    }

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @GetMapping("/")
    public String indexPage(@ModelAttribute(value = "LoggedUser") User user, SessionStatus status)
    {
        if(user == null)
            return "sections/index";
        else
        {
            if(!user.isBanned())
                return "redirect:/dashboard";
            else
            {
                status.setComplete(); // log out user
                return "handlingPages/youarebanned";
            }
        }
    }

    @GetMapping("/register")
    public String registerPage()
    {
        return "sections/registerPage";
    }

    @GetMapping("/dashboard")
    public String dashBoardPage(@ModelAttribute(value = "LoggedUser") User user, Model model, SessionStatus status)
    {
        if(user == null)
            return "redirect:/";
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

    @ModelAttribute(value = "LoggedUser")
    public User nullUser() {
        return null;
    }

    @GetMapping("/signin")
    public String signInPage(Model model, @ModelAttribute(value = "LoggedUser") User user)
    {

        if(user == null)
            return "sections/signInPage";
        else
            return "redirect:/";
    }

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