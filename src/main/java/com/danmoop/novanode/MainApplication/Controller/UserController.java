package com.danmoop.novanode.MainApplication.Controller;

import com.danmoop.novanode.MainApplication.Model.InboxMessage;
import com.danmoop.novanode.MainApplication.Model.Project;
import com.danmoop.novanode.MainApplication.Model.Task;
import com.danmoop.novanode.MainApplication.Model.User;
import com.danmoop.novanode.MainApplication.Service.ProjectService;
import com.danmoop.novanode.MainApplication.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

// This controller mostly manipulates with project tasks: completion, sending requests etc.
@Controller
@SessionAttributes(value = "LoggedUser")
public class UserController
{

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @PostMapping("/editProfileInfo")
    public String editProfileInfo(
            @ModelAttribute("LoggedUser") User user,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes)
    {

        User userDB = userService.findByUserName(user.getUserName());

        userDB.setName(name);
        userDB.setEmail(email);

        userService.save(userDB);

        redirectAttributes.addFlashAttribute("successMsg", "Info changed successfully!");

        return "redirect:/dashboard";
    }

    @PostMapping("/acceptRequest")
    public String requestAccepted(
            @ModelAttribute("LoggedUser") User user,
            @RequestParam("authorName") String authorName,
            @RequestParam("projectName") String projectName,
            @RequestParam("messageKey") String messageKey,
            RedirectAttributes redirectAttributes) throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        User userDB = userService.findByUserName(user.getUserName());
        User authorDB = userService.findByUserName(authorName);
        Project projectDB = projectService.findByName(projectName);

        if(userDB.isProjectAdmin(projectDB))
        {
            projectDB.addMember(authorName);

            projectDB.addMessage(new InboxMessage(user.getUserName() + " has accepted a new member - " + authorName, user.getUserName(), "inboxMessage"));

            authorDB.addProjectTakingPartIn(projectName);
            InboxMessage message = new InboxMessage(user.getUserName() + " has accepted your request in " + projectName + " project!", user.getUserName(), "inboxMessage");
            authorDB.addMessage(message);
            userDB.removeMessageFromInbox(userDB.findMessageByMessageKey(messageKey));

            userService.save(authorDB);
            userService.save(userDB);
            projectService.save(projectDB);

            redirectAttributes.addFlashAttribute("successMsg", authorName + "'s request accepted!");
        }

        return "redirect:/dashboard";
    }

    @PostMapping("/rejectRequest")
    public String requestRejected(
            @ModelAttribute("LoggedUser") User user,
            @RequestParam("authorName") String authorName,
            @RequestParam("projectName") String projectName,
            @RequestParam("messageKey") String messageKey,
            RedirectAttributes redirectAttributes) throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        User userDB = userService.findByUserName(user.getUserName());
        User authorDB = userService.findByUserName(authorName);

        InboxMessage message = new InboxMessage(user.getUserName() + " has rejected your request in " + projectName + " project.", user.getUserName(), "inboxMessage");

        authorDB.addMessage(message);

        userDB.removeMessageFromInbox(userDB.findMessageByMessageKey(messageKey));
        userService.save(authorDB);
        userService.save(userDB);

        redirectAttributes.addFlashAttribute("errorMsg", authorName + "'s request rejected");

        return "redirect:/dashboard";
    }

    @PostMapping("/acceptProjectInvite")
    public String acceptProjectInvite(
            @ModelAttribute("LoggedUser") User user,
            @RequestParam("authorName") String authorName,
            @RequestParam("projectName") String projectName,
            @RequestParam("messageKey") String messageKey,
            RedirectAttributes redirectAttributes) throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        User userDB = userService.findByUserName(user.getUserName());
        User authorDB = userService.findByUserName(authorName);
        Project projectDB = projectService.findByName(projectName);

        InboxMessage message = new InboxMessage(user.getUserName() + " has accepted your request in " + projectName + " project.", user.getUserName(), "inboxMessage");

        authorDB.addMessage(message);

        userDB.removeMessageFromInbox(userDB.findMessageByMessageKey(messageKey));

        projectDB.addMember(user.getUserName());

        projectDB.addMessage(new InboxMessage(authorName + " has accepted " + user.getUserName() + " to the project!", authorName, "inboxMessage"));

        userDB.addProjectTakingPartIn(projectName);

        userService.save(authorDB);
        userService.save(userDB);
        projectService.save(projectDB);

        redirectAttributes.addFlashAttribute("successMsg", authorName + "'s invite accepted");

        return "redirect:/dashboard";
    }

    @PostMapping("/rejectProjectInvite")
    public String rejectProjectInvite(
            @ModelAttribute("LoggedUser") User user,
            @RequestParam("authorName") String authorName,
            @RequestParam("projectName") String projectName,
            @RequestParam("messageKey") String messageKey,
            RedirectAttributes redirectAttributes) throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        User userDB = userService.findByUserName(user.getUserName());
        User authorDB = userService.findByUserName(authorName);

        InboxMessage message = new InboxMessage(user.getUserName() + " has rejected your request in " + projectName + " project.", user.getUserName(), "inboxMessage");

        authorDB.addMessage(message);

        userDB.removeMessageFromInbox(userDB.findMessageByMessageKey(messageKey));

        userService.save(authorDB);
        userService.save(userDB);

        redirectAttributes.addFlashAttribute("errorMsg", authorName + "'s invite rejected");

        return "redirect:/dashboard";
    }

    @PostMapping("/submitTaskReview")
    public String submitTaskReview(
            @ModelAttribute("LoggedUser") User user,
            @RequestParam("taskKey") String key,
            @RequestParam("taskMessage") String taskMessage,
            RedirectAttributes redirectAttributes) throws UnsupportedEncodingException, NoSuchAlgorithmException {

        Task task = user.findTaskByKey(key);

        Project projectDB = projectService.findByName(task.getProject());

        for(String admin: projectDB.getAdmins())
        {
            User adminUser = userService.findByUserName(admin);

            InboxMessage msg = new InboxMessage(user.getUserName() + " has requested task review.\n\nTask ID: " + task.getKey() + "\n\nTask description: " + task.getText()+"\n\nTask deadline: " + task.getDeadline() + "\n\n" + user.getUserName() + " has given details on this task: " + taskMessage, user.getUserName(), "inboxTaskRequest");

            msg.setDetails(key + "," + projectDB.getName());

            adminUser.addMessage(msg);

            userService.save(adminUser);
        }

        redirectAttributes.addFlashAttribute("successMsg", "Task review has been sent");

        return "redirect:/dashboard";
    }

    @PostMapping("/acceptTaskCompletion")
    public String acceptTaskCompletion(
            @RequestParam("messageText") String taskMessage,
            @RequestParam("workSuccess") String workSuccess,
            @RequestParam("taskKeyAndProjectName") String keyAndProj,
            @RequestParam("taskExecutor") String taskExecutor,
            @RequestParam("msgKey") String msgKey,
            @ModelAttribute("LoggedUser") User user,
            RedirectAttributes redirectAttributes ) throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        String key = keyAndProj.split(",")[0];
        String projectName = keyAndProj.split(",")[1];

        User executor = userService.findByUserName(taskExecutor);
        User userDB = userService.findByUserName(user.getUserName());
        Project projectDB = projectService.findByName(projectName);

        projectDB.addCompletedTask(projectDB.getTaskByKey(key));
        projectDB.removeTaskByKey(key);

        projectDB.addMessage(new InboxMessage(userDB.getUserName() + " has accepted " + executor.getUserName() + "'s task completion " + key + "\nWork success: " + workSuccess + " / 10", userDB.getUserName(), "inboxMessage"));

        userDB.removeMessageFromInbox(userDB.findMessageByMessageKey(msgKey));

        Task task = executor.findTaskByKey(key);

        executor.addWorkSuccessPoint(Integer.parseInt(workSuccess));
        executor.addCompletedTask(executor.findTaskByKey(key));
        executor.deleteTaskByKey(key);

        InboxMessage message = new InboxMessage(user.getUserName() + " has accepted your task " + key + " review. Good job!" + "\n\n" + user.getUserName() + "'s message to you: " + taskMessage + "\n\nTask details: " + task.getText() + "\n\nProject: " + task.getProject(), user.getUserName(), "inboxMessage");
        executor.addMessage(message);

        userService.save(executor);
        userService.save(userDB);

        projectService.save(projectDB);

        redirectAttributes.addFlashAttribute("successMsg", executor.getUserName() + "'s task completion accepted");

        return "redirect:/dashboard";
    }

    @PostMapping("/rejectTaskCompletion")
    public String rejectTaskCompletion(
            @RequestParam("taskExecutor") String taskExecutor,
            @RequestParam("taskKeyAndProjectName") String taskKeyAndProjectName,
            @RequestParam("msgKey") String msgKey,
            @RequestParam("messageText") String messageText,
            @ModelAttribute("LoggedUser") User user,
            RedirectAttributes redirectAttributes ) throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        String taskKey = taskKeyAndProjectName.split(",")[0];
        String projectName = taskKeyAndProjectName.split(",")[1];

        User userDB = userService.findByUserName(user.getUserName());

        User executor = userService.findByUserName(taskExecutor);

        userDB.removeMessageFromInbox(userDB.findMessageByMessageKey(msgKey));

        InboxMessage message = new InboxMessage(userDB.getUserName() + " has rejected your task review " + taskKey + " in " + projectName + " project. \nDetails on the task given: " + messageText, userDB.getUserName(), "inboxMessage");

        executor.addMessage(message);


        userService.save(userDB);
        userService.save(executor);

        redirectAttributes.addFlashAttribute("errorMsg", executor.getUserName() + "'s task completion rejected");

        return "redirect:/dashboard";
    }
}