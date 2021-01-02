package com.danmoop.novanode.MainApplication.controller;

import com.danmoop.novanode.MainApplication.model.InboxMessage;
import com.danmoop.novanode.MainApplication.model.Project;
import com.danmoop.novanode.MainApplication.model.Task;
import com.danmoop.novanode.MainApplication.model.User;
import com.danmoop.novanode.MainApplication.repository.ProjectRepository;
import com.danmoop.novanode.MainApplication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class TaskController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    /**
     * This request is handled when user sends a task review to project's admins
     * Admins will see this review request in their inbox
     *
     * @param auth        is a logged-in user object
     * @param key         is taken from a hidden html text field. Value is assigned using Thymeleaf
     * @param taskMessage is taken from a user's textarea. It is something user can say about task completion.
     * @return dashboard page. Send admins a task review message
     */
    @PostMapping("/submitTaskReview")
    public String submitTaskReview(
            Principal auth,
            @RequestParam("taskKey") String key,
            @RequestParam("taskMessage") String taskMessage,
            RedirectAttributes redirectAttributes) {

        User user = userRepository.findByUserName(auth.getName());
        Task task = user.findTaskByKey(key);
        Project projectDB = projectRepository.findByName(task.getProject());

        InboxMessage msg = new InboxMessage(auth.getName() + " has requested task review.\n\nTask ID: " + task.getKey() + "\n\nTask description: " + task.getText() + "\n\nTask deadline: " + task.getDeadline() + "\n\n" + auth.getName() + " has given details on this task: " + taskMessage, auth.getName(), "inboxTaskRequest");
        User taskGiver = userRepository.findByUserName(task.getAuthorName());
        msg.setDetails(key + "," + projectDB.getName());

        taskGiver.getMessages().add(msg);
        userRepository.save(taskGiver);

        redirectAttributes.addFlashAttribute("successMsg", "Task review has been sent");

        return "redirect:/dashboard";
    }

    /**
     * This request is handled when project admins accept task review that was sent before
     * It removes task from active tasks list, sends messages stating that the work is done
     *
     * @param workSuccess        is added to task executor's stats, it is used later for evaluating overall success
     *                           it is an integer from 1 to 10 meaning the value of completion (1-really bad, 10-excellent)
     * @param auth               is a logged-in user object
     * @param messageText        is taken from a user's textarea. It is something user can say about task completion
     * @param keyAndProj         has task key and project name, they are stored in a single string, then divided
     * @param taskExecutor       is a task executor's username
     * @param msgKey             is a message key
     * @param redirectAttributes is assigned automatically, it is used to display a message after redirect
     * @return dashboard page with all new information.
     */
    @PostMapping("/acceptTaskCompletion")
    public String acceptTaskCompletion(
            @RequestParam String messageText,
            @RequestParam String workSuccess,
            @RequestParam("taskKeyAndProjectName") String keyAndProj,
            @RequestParam String taskExecutor,
            @RequestParam String msgKey,
            Principal auth,
            RedirectAttributes redirectAttributes) {

        String key = keyAndProj.split(",")[0];
        String projectName = keyAndProj.split(",")[1];

        User executor = userRepository.findByUserName(taskExecutor);
        User userDB = userRepository.findByUserName(auth.getName());
        Project projectDB = projectRepository.findByName(projectName);

        projectDB.getCompletedTasks().add(projectDB.getTaskByKey(key));
        projectDB.removeTaskByKey(key);
        projectDB.getProjectInbox().add(new InboxMessage(userDB.getUserName() + " has accepted " + executor.getUserName() + "'s task completion " + key + "\nWork success: " + workSuccess + " / 10", userDB.getUserName(), "inboxMessage"));

        userDB.getMessages().remove(userDB.findMessageByMessageKey(msgKey));

        Task task = executor.findTaskByKey(key);

        executor.getWorkSuccessPoints().add(Integer.parseInt(workSuccess));
        executor.getCompletedTasks().add(executor.findTaskByKey(key));
        executor.deleteTaskByKey(key);

        InboxMessage message = new InboxMessage(auth.getName() + " has accepted your task " + key + " review. Good job!" + "\n\n" + auth.getName() + "'s message to you: " + messageText + "\n\nTask details: " + task.getText() + "\n\nProject: " + task.getProject(), auth.getName(), "inboxMessage");
        executor.getMessages().add(message);

        userRepository.save(executor);
        userRepository.save(userDB);
        projectRepository.save(projectDB);

        redirectAttributes.addFlashAttribute("successMsg", executor.getUserName() + "'s task completion accepted");

        return "redirect:/dashboard";
    }

    /**
     * This request is handled when project admins rejects task review that was sent before
     * Request sends a message with some advices to task executor
     *
     * @param auth               is a logged-in user object
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
            Principal auth,
            RedirectAttributes redirectAttributes) {

        String taskKey = taskKeyAndProjectName.split(",")[0];
        String projectName = taskKeyAndProjectName.split(",")[1];

        User userDB = userRepository.findByUserName(auth.getName());
        User executor = userRepository.findByUserName(taskExecutor);
        userDB.getMessages().remove(userDB.findMessageByMessageKey(msgKey));

        InboxMessage message = new InboxMessage(userDB.getUserName() + " has rejected your task review " + taskKey + " in " + projectName + " project. \nDetails on the task given: " + messageText, userDB.getUserName(), "inboxMessage");
        executor.getMessages().add(message);

        userRepository.save(userDB);
        userRepository.save(executor);

        redirectAttributes.addFlashAttribute("errorMsg", executor.getUserName() + "'s task completion rejected");

        return "redirect:/dashboard";
    }

    /**
     * This request is handled when project admin wants to add a new project task
     *
     * @param auth         is a logged-in user object
     * @param projectName  is a project name, taken from html text field
     * @param deadline     is a task deadline, it can be chosen from a calendar input on html page
     * @param description  is a task description
     * @param taskExecutor is a person who will be doing the task
     * @return project page with new task
     */
    @PostMapping("/addProjectTask")
    public String addProjectTask(
            Principal auth,
            @RequestParam("projectName") String projectName,
            @RequestParam("taskDeadline") String deadline,
            @RequestParam("taskExecutor") String taskExecutor,
            @RequestParam("taskDescription") String description,
            RedirectAttributes redirectAttributes) {
        Project projectDB = projectRepository.findByName(projectName);
        User executor = userRepository.findByUserName(taskExecutor);
        User user = userRepository.findByUserName(auth.getName());

        if (user.isProjectAdmin(projectDB) && executor != null) {
            if (!executor.getUserName().equals(user.getUserName())) {
                Task task = new Task(user.getUserName(), description, taskExecutor, deadline, projectName);
                InboxMessage notification = new InboxMessage(user.getUserName() + " created a new task, set " + executor.getUserName() + " (" + executor.getName() + ") as executor\n\nTask description: " + description + "\n\nTask ID: " + task.getKey() + "\n\nDeadline: " + deadline, user.getUserName(), "inboxMessage");
                InboxMessage message = new InboxMessage("Hello, " + executor.getName() + ". You have got a new task in " + projectName + " project.\n\nTask description: " + description + "\n\nTask ID: " + task.getKey() + "\n\nDeadline: " + deadline, user.getUserName(), "inboxMessage");

                projectDB.getProjectTasks().add(task);
                projectDB.getProjectInbox().add(notification);
                projectRepository.save(projectDB);

                executor.getTasks().add(task);
                executor.getMessages().add(message);
                userRepository.save(executor);
            } else {
                redirectAttributes.addFlashAttribute("errorMsg", "You can't give a task to yourself!");
            }
        }

        return "redirect:/project/" + projectName;
    }
}