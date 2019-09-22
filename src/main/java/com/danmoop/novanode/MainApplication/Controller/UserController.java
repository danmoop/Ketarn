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

// This controller mostly manipulates with project tasks: completion, sending requests etc.
@Controller
@SessionAttributes(value = "LoggedUser")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;


    /**
     * This request is handled when user wants to change their info - username and email
     *
     * @param user               is a logged-in user object
     * @param name               is taken from html textfield
     * @param email              is taken from html textfield
     * @param redirectAttributes is assigned automatically, it is used to display a message after redirect
     * @return dashboard page with a message added to @param redirectAttributes
     */
    @PostMapping("/editProfileInfo")
    public String editProfileInfo(
            @ModelAttribute("LoggedUser") User user,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes) {

        User userDB = userService.findByUserName(user.getUserName());

        userDB.setName(name);
        userDB.setEmail(email);

        userService.save(userDB);

        redirectAttributes.addFlashAttribute("successMsg", "Info changed successfully!");

        return "redirect:/dashboard";
    }


    /**
     * This request is handled when project's admin accepts the request sent by a user to join their project
     *
     * @param user        is a logged-in user object
     * @param authorName  is taken from html textfield
     * @param projectName is taken from html textfield
     * @param messageKey  is taken from a hidden html textfield. Value is assigned using Thymeleaf
     * @return dashboard page. Add member to project, add message about new member, save project and member objects
     */
    @PostMapping("/acceptRequest")
    public String requestAccepted(
            @ModelAttribute("LoggedUser") User user,
            @RequestParam("authorName") String authorName,
            @RequestParam("projectName") String projectName,
            @RequestParam("messageKey") String messageKey,
            RedirectAttributes redirectAttributes) {
        User userDB = userService.findByUserName(user.getUserName());
        User authorDB = userService.findByUserName(authorName);
        Project projectDB = projectService.findByName(projectName);

        if (userDB.isProjectAdmin(projectDB)) {
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


    /**
     * This request is handled when project's admin rejected the join request sent by a user
     *
     * @param user        is a logged-in user object
     * @param authorName  is taken from html textfield
     * @param projectName is taken from html textfield
     * @param messageKey  is taken from a hidden html textfield. Value is assigned using Thymeleaf
     * @return dashboard page. Send a rejection message and save user object
     */
    @PostMapping("/rejectRequest")
    public String requestRejected(
            @ModelAttribute("LoggedUser") User user,
            @RequestParam("authorName") String authorName,
            @RequestParam("projectName") String projectName,
            @RequestParam("messageKey") String messageKey,
            RedirectAttributes redirectAttributes) {
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


    /**
     * This request is handled when user accepts an invitation to a project sent before by project's admin
     *
     * @param user        is a logged-in user object
     * @param authorName  is taken from html textfield
     * @param projectName is taken from html textfield
     * @param messageKey  is taken from a hidden html textfield. Value is assigned using Thymeleaf
     * @return dashboard page. Add member to project, add message about new member, save project and member objects
     */
    @PostMapping("/acceptProjectInvite")
    public String acceptProjectInvite(
            @ModelAttribute("LoggedUser") User user,
            @RequestParam("authorName") String authorName,
            @RequestParam("projectName") String projectName,
            @RequestParam("messageKey") String messageKey,
            RedirectAttributes redirectAttributes) {
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


    /**
     * This request is handled when user rejects an invitation to a project sent before by project's admin
     *
     * @param user        is a logged-in user object
     * @param authorName  is taken from html textfield
     * @param projectName is taken from html textfield
     * @param messageKey  is taken from a hidden html textfield. Value is assigned using Thymeleaf
     * @return dashboard page and notify users about rejection
     */
    @PostMapping("/rejectProjectInvite")
    public String rejectProjectInvite(
            @ModelAttribute("LoggedUser") User user,
            @RequestParam("authorName") String authorName,
            @RequestParam("projectName") String projectName,
            @RequestParam("messageKey") String messageKey,
            RedirectAttributes redirectAttributes) {
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


    /**
     * This request is handled when user sends a task review to project's admins
     * Admins will see this review request in their inbox
     *
     * @param user        is a logged-in user object
     * @param key         is taken from a hidden html textfield. Value is assigned using Thymeleaf
     * @param taskMessage is taken from a user's textarea. It is something user can say about task completion.
     * @return dashboard page. Send admins a task review message
     */
    @PostMapping("/submitTaskReview")
    public String submitTaskReview(
            @ModelAttribute("LoggedUser") User user,
            @RequestParam("taskKey") String key,
            @RequestParam("taskMessage") String taskMessage,
            RedirectAttributes redirectAttributes) {

        Task task = user.findTaskByKey(key);

        Project projectDB = projectService.findByName(task.getProject());

        for (String admin : projectDB.getAdmins()) {
            User adminUser = userService.findByUserName(admin);

            InboxMessage msg = new InboxMessage(user.getUserName() + " has requested task review.\n\nTask ID: " + task.getKey() + "\n\nTask description: " + task.getText() + "\n\nTask deadline: " + task.getDeadline() + "\n\n" + user.getUserName() + " has given details on this task: " + taskMessage, user.getUserName(), "inboxTaskRequest");

            msg.setDetails(key + "," + projectDB.getName());

            adminUser.addMessage(msg);

            userService.save(adminUser);
        }

        redirectAttributes.addFlashAttribute("successMsg", "Task review has been sent");

        return "redirect:/dashboard";
    }

    /**
     * This request is handled when project admins accept task review that was sent before
     * It removes task from active tasks list, sends messages stating that the work is done
     *
     * @param workSuccess        is added to task executor's stats, it is used later for evaluating overall success
     * @param user               is a logged-in user object
     * @param taskMessage        is taken from a user's textarea. It is something user can say about task completion
     * @param workSuccess        is an integer from 1 to 10 meaning the value of completion (1-really bad, 10-excellent)
     * @param keyAndProj         has task key and project name, they are stored in a single string, then divided
     * @param taskExecutor       is a task executor's username
     * @param msgKey             is a message key
     * @param redirectAttributes is assigned automatically, it is used to display a message after redirect
     * @return dashboard page with all new information.
     */
    @PostMapping("/acceptTaskCompletion")
    public String acceptTaskCompletion(
            @RequestParam("messageText") String taskMessage,
            @RequestParam("workSuccess") String workSuccess,
            @RequestParam("taskKeyAndProjectName") String keyAndProj,
            @RequestParam("taskExecutor") String taskExecutor,
            @RequestParam("msgKey") String msgKey,
            @ModelAttribute("LoggedUser") User user,
            RedirectAttributes redirectAttributes) {
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

    /**
     * This request is handled when project admins rejects task review that was sent before
     * Request sends a message with some advices to task executor
     *
     * @param user               is a logged-in user object
     * @param taskExecutor       is a task executor's username
     * @param messageText        is a message sent by admin. There may be some advices how to do the job right
     * @param msgKey             is a message key
     * @param redirectAttributes is assigned automatically, it is used to display a message after redirect
     * @return dashboard page with all new information.
     */
    @PostMapping("/rejectTaskCompletion")
    public String rejectTaskCompletion(
            @RequestParam("taskExecutor") String taskExecutor,
            @RequestParam("taskKeyAndProjectName") String taskKeyAndProjectName,
            @RequestParam("msgKey") String msgKey,
            @RequestParam("messageText") String messageText,
            @ModelAttribute("LoggedUser") User user,
            RedirectAttributes redirectAttributes) {
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