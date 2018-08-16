package com.danmoop.novanode.MainApplication.Controller;

import com.danmoop.novanode.MainApplication.Model.*;
import com.danmoop.novanode.MainApplication.Service.ProjectService;
import com.danmoop.novanode.MainApplication.Service.UserService;
import com.danmoop.novanode.MainApplication.misc.Currency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Controller
@Document(collection = "projects")
@SessionAttributes("LoggedUser")

public class ProjectController
{
    @Autowired
    ProjectService projectService;

    @Autowired
    UserService userService;

    @GetMapping("/createProject")
    public String createProject(@ModelAttribute("LoggedUser") User user)
    {
        User userDB = userService.findByUserName(user.getUserName());

        if(userDB.isHasBoughtSubs())
            return "sections/createProject";
        else
            return "handlingPages/buysubs";
    }

    @PostMapping("/createProject")
    public String createProjectPOST(
            @ModelAttribute("LoggedUser") User user,
            @RequestParam("projectName") String projectName,
            @RequestParam("projectBudget") long projectBudget,
            @RequestParam("currencySign") String currencySign
    ) throws UnsupportedEncodingException, NoSuchAlgorithmException

    {
        if(projectService.findByName(projectName) == null)
        {
            Project project = new Project(projectName, user.getUserName(), projectBudget, currencySign);

            User userDB = userService.findByUserName(user.getUserName());

            userDB.addProject(projectName);
            userDB.addProjectTakingPartIn(projectName);
            project.addAdmin(userDB.getUserName());
            project.addMember(userDB.getUserName());

            userService.save(userDB);
            projectService.save(project);

            return "redirect:/project/" + projectName;
        }

        return "redirect:/dashboard";
    }

    @PostMapping("/setProjectNotification")
    public String notificationSubmitted(
            @ModelAttribute("LoggedUser") User user,
            @RequestParam("messageText") String text,
            @RequestParam("projectName") String projectName)
    {

        User userDB = userService.findByUserName(user.getUserName());
        Project projectDB = projectService.findByName(projectName);

        if(userDB.isAdmin(projectDB))
        {
            ProjectNotification projectNotification = new ProjectNotification(user.getUserName(), text);

            projectDB.setProjectNotification(projectNotification);

            projectService.save(projectDB);

            return "redirect:/project/" + projectName;
        }

        return "redirect:/dashboard";
    }

    @PostMapping("/setProjectBudget")
    public String setBudget(
            @ModelAttribute("LoggedUser") User user,
            @RequestParam("projectBudget") long budget,
            @RequestParam("projectName") String projectName,
            @RequestParam("budgetChangeCause") String reason) throws UnsupportedEncodingException, NoSuchAlgorithmException
    {

        User userDB = userService.findByUserName(user.getUserName());
        Project projectDB = projectService.findByName(projectName);

        if(userDB.isAdmin(projectDB))
        {

            String difference = moneyDifference(projectDB, projectDB.getBudget(), budget);

            InboxMessage notification = new InboxMessage(userDB.getUserName() + " has changed project budget from " + new Currency(projectDB.getBudget(), projectDB.getCurrencySign()).getFormattedAmount() + " to " + new Currency(budget, projectDB.getCurrencySign()).getFormattedAmount() + "\n\nReason: " + reason+"\n\nSummary: (" + difference+")", userDB.getUserName(), "inboxMessage");

            projectDB.addMessage(notification);

            projectDB.setBudget(budget);

            projectService.save(projectDB);

            return "redirect:/project/" + projectName;
        }

        return "redirect:/dashboard";
    }

    @PostMapping("/addProjectTask")
    public String addProjectTask(
            @ModelAttribute("LoggedUser") User user,
            @RequestParam("projectName") String projectName,
            @RequestParam("taskDeadline") String deadline,
            @RequestParam("taskExecutor") String taskExecutor,
            @RequestParam("taskDescription") String description) throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        Project projectDB = projectService.findByName(projectName);
        User executor = userService.findByUserName(taskExecutor);

        if(user.isAdmin(projectDB))
        {
            Task task = new Task(user.getUserName(), description, taskExecutor, deadline, projectName);
            InboxMessage notification = new InboxMessage(user.getUserName() + " created a new task, set " + executor.getUserName() + " (" + executor.getName() + ") as executor\n\nTask description: " + description + "\n\nTask ID: " + task.getKey() + "\n\nDeadline: " + deadline, user.getUserName(), "inboxMessage");
            projectDB.addTask(task);

            projectDB.addMessage(notification);

            projectService.save(projectDB);

            InboxMessage message = new InboxMessage("Hello, " + executor.getName() + ". You have got a new task in " + projectName + " project.\n\nTask description: " + description+"\n\nTask ID: " + task.getKey() + "\n\nDeadline: " + deadline, user.getUserName(), "inboxMessage");

            executor.addTask(task);
            executor.addMessage(message);
            userService.save(executor);
        }

        return "redirect:/project/" + projectName;
    }

    @PostMapping("/deleteAllInboxMessages")
    public String deleteAllInbox(@ModelAttribute("LoggedUser") User user, @RequestParam("projectName") String projectName, RedirectAttributes redirectAttributes) throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        Project projectDB = projectService.findByName(projectName);

        if(user.isAdmin(projectDB))
        {
            projectDB.emptyInbox();

            projectDB.addMessage(new InboxMessage(user.getUserName() + " has emptied project inbox.", user.getUserName(), "inboxMessage"));

            projectService.save(projectDB);

            redirectAttributes.addFlashAttribute("successMsg", "Project inbox cleared");

            return "redirect:/project/" + projectName;
        }

        return "redirect:/dashboard";
    }

    @GetMapping("/project/{projectName}")
    public String projectPage(@PathVariable String projectName, Model model, @ModelAttribute("LoggedUser") User user)
    {
        Project projectDB = projectService.findByName(projectName);
        User userDB = userService.findByUserName(user.getUserName());

        if(projectDB != null)
        {
            if (userDB.isMember(projectDB))
            {
                model.addAttribute("project", projectService.findByName(projectName));

                Currency budget = new Currency(projectDB.getBudget(), projectDB.getCurrencySign());

                model.addAttribute("budget", budget.getFormattedAmount());

                return "sections/projectDashboard";
            }
            else
            {
                model.addAttribute("projectName", projectName);
                return "handlingPages/notamember";
            }
        }

        return "error/404";
    }

    @PostMapping("/sendARequest")
    public String joinRequest(@RequestParam("projectName") String projectName, @RequestParam("userName") String userName, RedirectAttributes redirectAttributes) throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        Project projectDB = projectService.findByName(projectName);

        List<String> admins = projectDB.getAdmins();

        for (String admin1 : admins)
        {
            User admin = userService.findByUserName(admin1);

            InboxMessage message = new InboxMessage(userName + " wants to join " + projectName + " project. \nSubmit this request or reject.", userName, "inboxRequest");

            message.setDetails(projectName);

            admin.addMessage(message);

            userService.save(admin);
        }

        redirectAttributes.addFlashAttribute("successMsg", "Request sent successfully to " + projectName + " project!");

        return "redirect:/dashboard";
    }

    @PostMapping("/inviteMemberToProject")
    public String inviteMemberToProject(
            @ModelAttribute("LoggedUser") User user,
            @RequestParam("projectName") String projectName,
            @RequestParam("memberName") String recepient,
            RedirectAttributes redirectAttributes) throws UnsupportedEncodingException, NoSuchAlgorithmException
    {

        InboxMessage message = new InboxMessage(user.getUserName() + " has invited you to join " + projectName + " project. Accept this invite or reject.", user.getUserName(), "inboxRequestToMember");

        User userRecepient = userService.findByUserName(recepient);

        if(userRecepient != null)
        {
            message.setDetails(projectName);

            userRecepient.addMessage(message);

            userService.save(userRecepient);

            redirectAttributes.addFlashAttribute("successMsg", recepient + " has been invited!");
        }

        else
        {
            redirectAttributes.addFlashAttribute("errorMsg", "There is no user with such username: " + recepient);
        }


        return "redirect:/project/" + projectName;
    }

    @PostMapping("/setMemberAsAdmin")
    public String setAsAdmin(
            @RequestParam("projectName") String projectName,
            @RequestParam("memberName") String memberName,
            @ModelAttribute("LoggedUser") User user,
            RedirectAttributes redirectAttributes) throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        Project projectDB = projectService.findByName(projectName);
        User member = userService.findByUserName(memberName);

        if(user.isAdmin(projectDB) && !projectDB.getAdmins().contains(memberName))
        {
            InboxMessage message = new InboxMessage(user.getUserName() + " has set " + memberName + " as project admin", user.getUserName(), "inboxMessage");

            projectDB.addMessage(message);
            projectDB.addAdmin(memberName);
            projectService.save(projectDB);

            InboxMessage userMessage = new InboxMessage(user.getUserName() + " has set you as " + projectName + "'s admin", user.getUserName(), "inboxMessage");
            member.addMessage(userMessage);
            userService.save(member);

            redirectAttributes.addFlashAttribute("successMsg", memberName + " has been set as " + projectName + "'s admin");
        }

        return "redirect:/project/" + projectName;

    }

    private String moneyDifference(Project project, long before, long after)
    {
        if(after - before > 0)
            return "+ " + new Currency(after - before, project.getCurrencySign()).getFormattedAmount();
        else
            return "- " + new Currency(before - after, project.getCurrencySign()).getFormattedAmount();
    }
}