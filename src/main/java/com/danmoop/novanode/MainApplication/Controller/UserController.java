package com.danmoop.novanode.MainApplication.Controller;

import com.danmoop.novanode.MainApplication.Model.InboxMessage;
import com.danmoop.novanode.MainApplication.Model.Project;
import com.danmoop.novanode.MainApplication.Model.Task;
import com.danmoop.novanode.MainApplication.Model.User;
import com.danmoop.novanode.MainApplication.Service.ProjectService;
import com.danmoop.novanode.MainApplication.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.stream.Collectors;

/**
 * @class UserController
 * This class mostly manipulates with project tasks: completion, sending requests etc.
 */
@Controller
@SessionAttributes(value = "LoggedUser")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * This request is handled when user wants to change their info - username and email
     *
     * @param principal          is a logged-in user object
     * @param name               is taken from html textfield
     * @param email              is taken from html textfield
     * @param redirectAttributes is assigned automatically, it is used to display a message after redirect
     * @return dashboard page with a message added to @param redirectAttributes
     */
    @PostMapping("/editProfileInfo")
    public String editProfileInfo(
            Principal principal,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes) {

        User userDB = userService.findByUserName(principal.getName());

        userDB.setName(name);
        userDB.setEmail(email);
        userService.save(userDB);

        redirectAttributes.addFlashAttribute("successMsg", "Info changed successfully!");

        return "redirect:/dashboard";
    }

    /**
     * This request is handled when user wants to change password
     *
     * @param principal   is a logged-in user object
     * @param oldPass     is an old password, taken from html input field
     * @param newPass     is a new password, taken from html input field
     * @param confirmPass is a new password, taken from html input field
     * @return dashboard page
     */
    @PostMapping("/changePassword")
    public String changePassword(
            Principal principal,
            @RequestParam("old_pass") String oldPass,
            @RequestParam("new_pass") String newPass,
            @RequestParam("confirm_pass") String confirmPass,
            RedirectAttributes redirectAttributes) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        User userDB = userService.findByUserName(principal.getName());

        /*
            We compare an old password to a current password, so with new pass and confirmation
            If everything is fine, we proceed and rewrite user's information
        */
        if (passwordEncoder.encode(oldPass).equals(userDB.getPassword()) && newPass.equals(confirmPass)) {
            userDB.setPassword(passwordEncoder.encode(newPass));
            userService.save(userDB);

            redirectAttributes.addFlashAttribute("successMsg", "Password changed successfully!");

            return "redirect:/dashboard";
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "Unable to change password, passwords don't match");
            return "redirect:/dashboard";
        }

    }

    /**
     * This request is handled when project's admin accepts the request sent by a user to join their project
     *
     * @param principal   is a logged-in user object
     * @param authorName  is taken from html textfield
     * @param projectName is taken from html textfield
     * @param messageKey  is taken from a hidden html textfield. Value is assigned using Thymeleaf
     * @return dashboard page. Add member to project, add message about new member, save project and member objects
     */
    @PostMapping("/acceptRequest")
    public String requestAccepted(
            Principal principal,
            @RequestParam("authorName") String authorName,
            @RequestParam("projectName") String projectName,
            @RequestParam("messageKey") String messageKey,
            RedirectAttributes redirectAttributes) {

        User userDB = userService.findByUserName(principal.getName());
        User authorDB = userService.findByUserName(authorName);
        Project projectDB = projectService.findByName(projectName);

        if (userDB.isProjectAdmin(projectDB)) {
            projectDB.addMember(authorName);

            projectDB.addMessage(new InboxMessage(principal.getName() + " has accepted a new member - " + authorName, principal.getName(), "inboxMessage"));

            authorDB.addProjectTakingPartIn(projectName);
            InboxMessage message = new InboxMessage(principal.getName() + " has accepted your request in " + projectName + " project!", principal.getName(), "inboxMessage");
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
     * @param principal   is a logged-in user object
     * @param authorName  is taken from html textfield
     * @param projectName is taken from html textfield
     * @param messageKey  is taken from a hidden html textfield. Value is assigned using Thymeleaf
     * @return dashboard page. Send a rejection message and save user object
     */
    @PostMapping("/rejectRequest")
    public String requestRejected(
            Principal principal,
            @RequestParam("authorName") String authorName,
            @RequestParam("projectName") String projectName,
            @RequestParam("messageKey") String messageKey,
            RedirectAttributes redirectAttributes) {

        User userDB = userService.findByUserName(principal.getName());
        User authorDB = userService.findByUserName(authorName);

        InboxMessage message = new InboxMessage(principal.getName() + " has rejected your request in " + projectName + " project.", principal.getName(), "inboxMessage");

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
     * @param principal   is a logged-in user object
     * @param authorName  is taken from html textfield
     * @param projectName is taken from html textfield
     * @param messageKey  is taken from a hidden html textfield. Value is assigned using Thymeleaf
     * @return dashboard page. Add member to project, add message about new member, save project and member objects
     */
    @PostMapping("/acceptProjectInvite")
    public String acceptProjectInvite(
            Principal principal,
            @RequestParam("authorName") String authorName,
            @RequestParam("projectName") String projectName,
            @RequestParam("messageKey") String messageKey,
            RedirectAttributes redirectAttributes) {

        User userDB = userService.findByUserName(principal.getName());
        User authorDB = userService.findByUserName(authorName);
        Project projectDB = projectService.findByName(projectName);

        InboxMessage message = new InboxMessage(principal.getName() + " has accepted your request in " + projectName + " project.", principal.getName(), "inboxMessage");
        authorDB.addMessage(message);

        userDB.removeMessageFromInbox(userDB.findMessageByMessageKey(messageKey));

        projectDB.addMember(principal.getName());
        projectDB.addMessage(new InboxMessage(authorName + " has accepted " + principal.getName() + " to the project!", authorName, "inboxMessage"));

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
     * @param principal   is a logged-in user object
     * @param authorName  is taken from html textfield
     * @param projectName is taken from html textfield
     * @param messageKey  is taken from a hidden html textfield. Value is assigned using Thymeleaf
     * @return dashboard page and notify users about rejection
     */
    @PostMapping("/rejectProjectInvite")
    public String rejectProjectInvite(
            Principal principal,
            @RequestParam("authorName") String authorName,
            @RequestParam("projectName") String projectName,
            @RequestParam("messageKey") String messageKey,
            RedirectAttributes redirectAttributes) {

        User userDB = userService.findByUserName(principal.getName());
        User authorDB = userService.findByUserName(authorName);

        InboxMessage message = new InboxMessage(principal.getName() + " has rejected your request in " + projectName + " project.", principal.getName(), "inboxMessage");
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
     * @param principal   is a logged-in user object
     * @param key         is taken from a hidden html textfield. Value is assigned using Thymeleaf
     * @param taskMessage is taken from a user's textarea. It is something user can say about task completion.
     * @return dashboard page. Send admins a task review message
     */
    @PostMapping("/submitTaskReview")
    public String submitTaskReview(
            Principal principal,
            @RequestParam("taskKey") String key,
            @RequestParam("taskMessage") String taskMessage,
            RedirectAttributes redirectAttributes) {

        User user = userService.findByUserName(principal.getName());
        Task task = user.findTaskByKey(key);
        Project projectDB = projectService.findByName(task.getProject());

        InboxMessage msg = new InboxMessage(principal.getName() + " has requested task review.\n\nTask ID: " + task.getKey() + "\n\nTask description: " + task.getText() + "\n\nTask deadline: " + task.getDeadline() + "\n\n" + principal.getName() + " has given details on this task: " + taskMessage, principal.getName(), "inboxTaskRequest");

        projectDB.getAdmins().stream()
                .map(adminName -> userService.findByUserName(adminName))
                .collect(Collectors.toList())
                .forEach(admin -> {
                    msg.setDetails(key + "," + projectDB.getName());

                    admin.addMessage(msg);
                    userService.save(admin);
                });

        redirectAttributes.addFlashAttribute("successMsg", "Task review has been sent");

        return "redirect:/dashboard";
    }

    /**
     * This request is handled when project admins accept task review that was sent before
     * It removes task from active tasks list, sends messages stating that the work is done
     *
     * @param workSuccess        is added to task executor's stats, it is used later for evaluating overall success
     * @param principal          is a logged-in user object
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
            Principal principal,
            RedirectAttributes redirectAttributes) {

        String key = keyAndProj.split(",")[0];
        String projectName = keyAndProj.split(",")[1];

        User executor = userService.findByUserName(taskExecutor);
        User userDB = userService.findByUserName(principal.getName());
        Project projectDB = projectService.findByName(projectName);

        projectDB.addCompletedTask(projectDB.getTaskByKey(key));
        projectDB.removeTaskByKey(key);
        projectDB.addMessage(new InboxMessage(userDB.getUserName() + " has accepted " + executor.getUserName() + "'s task completion " + key + "\nWork success: " + workSuccess + " / 10", userDB.getUserName(), "inboxMessage"));

        userDB.removeMessageFromInbox(userDB.findMessageByMessageKey(msgKey));

        Task task = executor.findTaskByKey(key);

        executor.addWorkSuccessPoint(Integer.parseInt(workSuccess));
        executor.addCompletedTask(executor.findTaskByKey(key));
        executor.deleteTaskByKey(key);

        InboxMessage message = new InboxMessage(principal.getName() + " has accepted your task " + key + " review. Good job!" + "\n\n" + principal.getName() + "'s message to you: " + taskMessage + "\n\nTask details: " + task.getText() + "\n\nProject: " + task.getProject(), principal.getName(), "inboxMessage");
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
     * @param principal          is a logged-in user object
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
            Principal principal,
            RedirectAttributes redirectAttributes) {

        String taskKey = taskKeyAndProjectName.split(",")[0];
        String projectName = taskKeyAndProjectName.split(",")[1];

        User userDB = userService.findByUserName(principal.getName());
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