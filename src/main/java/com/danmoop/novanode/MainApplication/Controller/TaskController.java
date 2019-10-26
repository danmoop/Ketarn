package com.danmoop.novanode.MainApplication.Controller;

import com.danmoop.novanode.MainApplication.Model.InboxMessage;
import com.danmoop.novanode.MainApplication.Model.Project;
import com.danmoop.novanode.MainApplication.Model.Task;
import com.danmoop.novanode.MainApplication.Model.User;
import com.danmoop.novanode.MainApplication.Service.ProjectService;
import com.danmoop.novanode.MainApplication.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class TaskController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;


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
        User taskGiver = userService.findByUserName(task.getAuthorName());
        msg.setDetails(key + "," + projectDB.getName());

        taskGiver.addMessage(msg);
        userService.save(taskGiver);

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


    /**
     * This request is handled when project admin wants to add a new project task
     *
     * @param principal    is a logged-in user object
     * @param projectName  is a project name, taken from html textfield
     * @param deadline     is a task deadline, it can be chosen from a calendar input on html page
     * @param description  is a task description
     * @param taskExecutor is a person who will be doing the task
     * @return project page with new task
     * @see InboxMessage
     */
    @PostMapping("/addProjectTask")
    public String addProjectTask(
            Principal principal,
            @RequestParam("projectName") String projectName,
            @RequestParam("taskDeadline") String deadline,
            @RequestParam("taskExecutor") String taskExecutor,
            @RequestParam("taskDescription") String description,
            RedirectAttributes redirectAttributes) {
        Project projectDB = projectService.findByName(projectName);
        User executor = userService.findByUserName(taskExecutor);
        User user = userService.findByUserName(principal.getName());

        if (user.isProjectAdmin(projectDB) && executor != null) {
            if (!executor.getUserName().equals(user.getUserName())) {
                Task task = new Task(user.getUserName(), description, taskExecutor, deadline, projectName);
                InboxMessage notification = new InboxMessage(user.getUserName() + " created a new task, set " + executor.getUserName() + " (" + executor.getName() + ") as executor\n\nTask description: " + description + "\n\nTask ID: " + task.getKey() + "\n\nDeadline: " + deadline, user.getUserName(), "inboxMessage");
                InboxMessage message = new InboxMessage("Hello, " + executor.getName() + ". You have got a new task in " + projectName + " project.\n\nTask description: " + description + "\n\nTask ID: " + task.getKey() + "\n\nDeadline: " + deadline, user.getUserName(), "inboxMessage");

                projectDB.addTask(task);
                projectDB.addMessage(notification);
                projectService.save(projectDB);

                executor.addTask(task);
                executor.addMessage(message);
                userService.save(executor);
            } else {
                redirectAttributes.addFlashAttribute("errorMsg", "You can't give a task to yourself!");
            }
        }
        return "redirect:/project/" + projectName;
    }
}