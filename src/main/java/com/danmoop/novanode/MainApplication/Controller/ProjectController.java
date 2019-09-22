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

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Document(collection = "projects")
@SessionAttributes("LoggedUser")

public class ProjectController {
    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;


    /**
     * This request is handled when user redirects to project creation page
     *
     * @return project creation html page
     */
    @GetMapping("/createProject")
    public String createProject(@ModelAttribute("LoggedUser") User user) {
        return "sections/createProject";
    }


    /**
     * This request is handled when user fills in project info and submits it
     * Project is created and saved to database
     *
     * @param user          is a logged-in user object
     * @param projectBudget is a project budget. It is taken from html textfield
     * @param projectName   is a project name, taken from html textfield
     * @param currencySign  is a budget currency sign (USD, EUR...), taken from html select field
     * @return dashboard page with new project and other user's information
     */
    @PostMapping("/createProject")
    public String createProjectPOST(
            @ModelAttribute("LoggedUser") User user,
            @RequestParam("projectName") String projectName,
            @RequestParam("projectBudget") long projectBudget,
            @RequestParam("currencySign") String currencySign
    ) {
        if (projectService.findByName(projectName) == null) {
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


    /**
     * @param user        is a logged-in user object
     * @param projectName is a project name, taken from html textfield
     * @param text        is a notification text message
     * @return project page with new notification
     * @see ProjectNotification
     * 
     * This request is handled when project admin wants to set a notification for other members
     */
    @PostMapping("/setProjectNotification")
    public String notificationSubmitted(
            @ModelAttribute("LoggedUser") User user,
            @RequestParam("messageText") String text,
            @RequestParam("projectName") String projectName) {
        User userDB = userService.findByUserName(user.getUserName());
        Project projectDB = projectService.findByName(projectName);

        if (userDB.isProjectAdmin(projectDB)) {
            ProjectNotification projectNotification = new ProjectNotification(user.getUserName(), text);

            projectDB.setProjectNotification(projectNotification);

            projectService.save(projectDB);

            return "redirect:/project/" + projectName;
        }

        return "redirect:/dashboard";
    }


    /**
     * @param user        is a logged-in user object
     * @param projectName is a project name, taken from html textfield
     * @param budget      is a new project budget
     * @param reason      states why budget is changed. It will be seen to everybody
     * @return project page with new budget
     * @see InboxMessage
     * 
     * This request is handled when project admin wants to set a new project budget
     */
    @PostMapping("/setProjectBudget")
    public String setBudget(
            @ModelAttribute("LoggedUser") User user,
            @RequestParam("projectBudget") long budget,
            @RequestParam("projectName") String projectName,
            @RequestParam("budgetChangeCause") String reason) {

        User userDB = userService.findByUserName(user.getUserName());
        Project projectDB = projectService.findByName(projectName);

        if (userDB.isProjectAdmin(projectDB)) {
            String difference = moneyDifference(projectDB, projectDB.getBudget(), budget);

            InboxMessage notification = new InboxMessage(userDB.getUserName() + " has changed project budget from " + new Currency(projectDB.getBudget(), projectDB.getCurrencySign()).getFormattedAmount() + " to " + new Currency(budget, projectDB.getCurrencySign()).getFormattedAmount() + "\n\nReason: " + reason + "\n\nSummary: (" + difference + ")", userDB.getUserName(), "inboxMessage");

            projectDB.addMessage(notification);

            projectDB.setBudget(budget);

            projectService.save(projectDB);

            return "redirect:/project/" + projectName;
        }

        return "redirect:/dashboard";
    }

    /**
     * @param user         is a logged-in user object
     * @param projectName  is a project name, taken from html textfield
     * @param deadline     is a task deadline, it can be chosen from a calendar input on html page
     * @param description  is a task description
     * @param taskExecutor is a person who will be doing the task
     * @return project page with new task
     * @see InboxMessage
     * 
     * This request is handled when project admin wants to add a new project task
     */
    @PostMapping("/addProjectTask")
    public String addProjectTask(
            @ModelAttribute("LoggedUser") User user,
            @RequestParam("projectName") String projectName,
            @RequestParam("taskDeadline") String deadline,
            @RequestParam("taskExecutor") String taskExecutor,
            @RequestParam("taskDescription") String description,
            RedirectAttributes redirectAttributes) {
        Project projectDB = projectService.findByName(projectName);
        User executor = userService.findByUserName(taskExecutor);

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
            } else
                redirectAttributes.addFlashAttribute("errorMsg", "You can't give a task to yourself. Only other member can do that");
        }
        return "redirect:/project/" + projectName;
    }

    /**
     * @param user        is a logged-in user object
     * @param projectName is a project name, taken from html textfield
     * @return project page with an empty inbox
     * @see InboxMessage
     * 
     * This request is handled when project admin wants to clear project inbox
     * Actually it will be cleared, but the only 1 message will remain - saying who cleared it
     */
    @PostMapping("/deleteAllInboxMessages")
    public String deleteAllInbox(@ModelAttribute("LoggedUser") User user, @RequestParam("projectName") String projectName, RedirectAttributes redirectAttributes) {
        Project projectDB = projectService.findByName(projectName);

        if (user.isProjectAdmin(projectDB) && projectDB != null) {
            projectDB.emptyInbox();
            projectDB.addMessage(new InboxMessage(user.getUserName() + " has emptied project inbox.", user.getUserName(), "inboxMessage"));
            projectService.save(projectDB);

            redirectAttributes.addFlashAttribute("successMsg", "Project inbox cleared");

            return "redirect:/project/" + projectName;
        }

        return "redirect:/dashboard";
    }

    /**
     * @param user        is a logged-in user object
     * @param projectName is a project name, taken from address bar (like /project/SpringFramework)
     * @return project page if user is a member of the project, otherwise redirect to not-a-member page
     * @see Currency
     * 
     * This request is handled when user wants to open a project page
     */
    @GetMapping("/project/{projectName}")
    public String projectPage(@PathVariable String projectName, Model model, @ModelAttribute("LoggedUser") User user) {
        Project projectDB = projectService.findByName(projectName);
        User userDB = userService.findByUserName(user.getUserName());

        if (projectDB != null && userDB != null) {
            if (userDB.isMember(projectDB)) {
                model.addAttribute("project", projectService.findByName(projectName));

                Currency budget = new Currency(projectDB.getBudget(), projectDB.getCurrencySign());
                model.addAttribute("budget", budget.getFormattedAmount());

                return "sections/projectDashboard";
            } else {
                model.addAttribute("projectName", projectName);
                return "handlingPages/notamember";
            }
        }

        return "error/404";
    }


    /**
     * This request is handled when user wants to open a project chat
     *
     * @param user        is a logged-in user object
     * @param projectName is a project name, taken from address bar (like /project/SpringFramework)
     * @return project chat page
     */
    @GetMapping("/project/{projectName}/chat")
    public String projectChat(
            @PathVariable("projectName") String projectName,
            @ModelAttribute("LoggedUser") User user,
            Model model) {
        Project project = projectService.findByName(projectName);

        if (project != null && project.getMembers().contains(user.getUserName()))
            model.addAttribute("project", project);
        else
            return "redirect:/dashboard";

        return "sections/projectChat";
    }

    /**
     * @param projectName is a project name, taken from address bar (like /project/SpringFramework)
     * @param userName    is a name of the user who wants to join the project
     * @return dashboard page
     * @see InboxMessage
     * 
     * This request is handled when user opened a project page and saw that they are not a part of project's team
     * They can press a button -> 'send request to join' and all project admins will get that request
     */
    @PostMapping("/sendARequest")
    public String joinRequest(
            @RequestParam("projectName") String projectName,
            @RequestParam("userName") String userName,
            RedirectAttributes redirectAttributes) {
        Project projectDB = projectService.findByName(projectName);
        List<String> admins = projectDB.getAdmins();
        InboxMessage message = new InboxMessage(userName + " wants to join " + projectName + " project. \nAccept this request or reject.", userName, "inboxRequest");

        for (String admin1 : admins) {
            User admin = userService.findByUserName(admin1);

            message.setDetails(projectName);
            admin.addMessage(message);

            userService.save(admin);
        }

        redirectAttributes.addFlashAttribute("successMsg", "Request sent successfully to " + projectName + " project!");

        return "redirect:/dashboard";
    }


    /**
     * @param user        is a logged-in user object
     * @param projectName is a project name, taken from a hidden html input field
     * @param recipient   is the user who gets the invitation
     * @return project page
     * @see InboxMessage
     * 
     * This request is handled when project admin wants to invite specific user to the project
     */
    @PostMapping("/inviteMemberToProject")
    public String inviteMemberToProject(
            @ModelAttribute("LoggedUser") User user,
            @RequestParam("projectName") String projectName,
            @RequestParam("memberName") String recipient,
            RedirectAttributes redirectAttributes) {

        InboxMessage message = new InboxMessage(user.getUserName() + " has invited you to join " + projectName + " project. Accept this invite or reject.", user.getUserName(), "inboxRequestToMember");

        User userRecipient = userService.findByUserName(recipient);

        if (userRecipient != null) {
            message.setDetails(projectName);
            userRecipient.addMessage(message);
            userService.save(userRecipient);

            redirectAttributes.addFlashAttribute("successMsg", recipient + " has been invited!");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "There is no user with such username: " + recipient);
        }


        return "redirect:/project/" + projectName;
    }


    /**
     * @param user        is a logged-in user object
     * @param projectName is a project name, taken from a hidden html input field
     * @param memberName  is a name of the user who becomes an admin
     * @return project page
     * @see InboxMessage
     * 
     * This request is handled when project admin wants to make another user admin
     */
    @PostMapping("/setMemberAsAdmin")
    public String setAsAdmin(
            @RequestParam("projectName") String projectName,
            @RequestParam("memberName") String memberName,
            @ModelAttribute("LoggedUser") User user,
            RedirectAttributes redirectAttributes) {
        Project projectDB = projectService.findByName(projectName);
        User member = userService.findByUserName(memberName);

        if (user.isProjectAdmin(projectDB) && !projectDB.getAdmins().contains(memberName) && member != null) {
            InboxMessage message = new InboxMessage(user.getUserName() + " has set " + memberName + " as project admin", user.getUserName(), "inboxMessage");

            projectDB.addMessage(message);
            projectDB.addAdmin(memberName);
            projectService.save(projectDB);

            InboxMessage userMessage = new InboxMessage(user.getUserName() + " has set you as " + projectName + "'s admin", user.getUserName(), "inboxMessage");
            member.addMessage(userMessage);
            userService.save(member);

            redirectAttributes.addFlashAttribute("successMsg", memberName + " has been set as " + projectName + "'s admin");
        } else
            redirectAttributes.addFlashAttribute("errorMsg", "An error has occurred");

        return "redirect:/project/" + projectName;
    }


    /**
     * @param user         is a logged-in user object
     * @param projectName  is a project name, taken from a hidden html input field
     * @param currentAdmin is a name of the user who no longer will be an admin
     * @return project page
     * @see InboxMessage
     * 
     * This request is handled when project admin wants to take another user's admin right
     * That user will no longer be an admin
     */
    @PostMapping("/unAdmin")
    public String unAdminUser(
            @RequestParam("projectName") String projectName,
            @RequestParam("currentAdmin") String currentAdmin,
            @ModelAttribute("LoggedUser") User user,
            RedirectAttributes redirectAttributes) {
        Project projectDB = projectService.findByName(projectName);

        if (projectDB != null && projectDB.getAdmins().contains(user.getUserName()) && !user.getUserName().equals(currentAdmin)) {
            projectDB.removeAdmin(currentAdmin);

            InboxMessage message = new InboxMessage(user.getUserName() + " has taken admin right from " + currentAdmin, user.getUserName(), "inboxMessage");
            projectDB.addMessage(message);

            projectService.save(projectDB);

            redirectAttributes.addFlashAttribute("successMsg", currentAdmin + " is not an admin anymore!");
        } else
            redirectAttributes.addFlashAttribute("errorMsg", "An error has occurred");

        return "redirect:/project/" + projectName;
    }


    /**
     * @param user          is a logged-in user object
     * @param projectName   is a project name, taken from a hidden html input field
     * @param directoryName is a directory name where that item belongs
     * @return project page
     * @see ProjectItem
     * This request is handled when project admin wants to add another project item to project
     * This item will be added to project to it's directory and saved
     */
    @PostMapping("/addNewCard")
    public String newCard(
            @RequestParam("projectName") String projectName,
            @RequestParam("itemText") String itemText,
            @RequestParam("directoryName") String directoryName,
            @ModelAttribute("LoggedUser") User user) {
        ProjectItem projectItem = new ProjectItem(itemText, projectName);
        Project project = projectService.findByName(projectName);

        if (project.getAdmins().contains(user.getUserName())) {
            project.addItem(projectItem, directoryName);

            projectService.save(project);
        }

        return "redirect:/project/" + projectName;
    }

    /**
     * @param user        is a logged-in user object
     * @param projectName is a project name, taken from a hidden html input field
     * @return project page
     * @see ProjectItem
     * 
     * This request is handled when project admin wants to remove a project item
     * It will be removed
     */
    @PostMapping("/removeItem")
    public String removeCard(
            @RequestParam("projectName") String projectName,
            @RequestParam("itemKey") String cardKey,
            @ModelAttribute("LoggedUser") User user
    ) {
        Project project = projectService.findByName(projectName);

        if (project != null && project.getAdmins().contains(user.getUserName())) {
            ProjectItem projectItem = project.getItemByKey(cardKey);

            project.removeCard(projectItem);

            projectService.save(project);
        }

        return "redirect:/project/" + projectName;
    }

    /**
     * @param user        is a logged-in user object
     * @param projectName is a project name, taken from a hidden html input field
     * @return project page
     * @see ProjectItem
     * 
     * This request is handled when project admin wants to mark the item as done
     * The item will be moved to 'done' category
     */
    @PostMapping("/markItemAsDone")
    public String markItemAsDone(
            @RequestParam("projectName") String projectName,
            @RequestParam("itemKey") String itemKey,
            @ModelAttribute("LoggedUser") User user
    ) {
        Project project = projectService.findByName(projectName);

        if (project != null && project.getAdmins().contains(user.getUserName())) {
            ProjectItem projectItem = project.getItemByKey(itemKey);

            project.removeCard(projectItem);
            project.addItem(projectItem, "done");
            projectService.save(project);
        }

        return "redirect:/project/" + projectName;
    }


    /**
     * This request is handled when project admin wants to mark all current items as done
     * All of them will be moved to 'done' category
     *
     * @param user        is a logged-in user object
     * @param projectName is a project name, taken from a hidden html input field
     * @return project page
     */
    @PostMapping("/currentItemsAllDone")
    public String currentItemsAllDone(@RequestParam("projectName") String projectName, @ModelAttribute("LoggedUser") User user) {
        Project project = projectService.findByName(projectName);

        if (project != null && project.getAdmins().contains(user.getUserName())) {
            project.markAllCurrentItemsAsDone();
            projectService.save(project);
        }

        return "redirect:/project/" + projectName;
    }


    /**
     * This request is handled when project admin wants to mark all done items as done
     * All of them will be removed
     *
     * @param user        is a logged-in user object
     * @param projectName is a project name, taken from a hidden html input field
     * @return project page
     */
    @PostMapping("/removeDoneItems")
    public String removeDoneItems(@RequestParam("projectName") String projectName, @ModelAttribute("LoggedUser") User user) {
        Project project = projectService.findByName(projectName);

        if (project != null && project.getAdmins().contains(user.getUserName())) {
            project.removeAllDoneItems();
            projectService.save(project);
        }

        return "redirect:/project/" + projectName;
    }


    /**
     * This request is handled when project member wants to send a message to chat
     * It will be saved and visible to everyone else in the chat
     *
     * @param user        is a logged-in user object
     * @param projectName is a project name, taken from a hidden html input field
     * @return project page
     */
    @PostMapping("/sendMessageToChat")
    public String sendMessageToChat(@RequestParam("projectName") String projectName, @RequestParam("message") String message, @ModelAttribute("LoggedUser") User user) {
        Project project = projectService.findByName(projectName);

        if (project != null && project.getMembers().contains(user.getUserName()) && !message.equals("")) {
            ChatMessage chatMessage = new ChatMessage(message, user.getUserName(), new Date().toString());
            project.addChatMessage(chatMessage);

            projectService.save(project);
        }

        return "redirect:/project/" + projectName + "/chat";
    }


    /**
     * @param user        is a logged-in user object
     * @param projectName is a project name, taken from a hidden html input field
     * @return dashboard page
     * @see InboxMessage
     * This request is handled when project admin wants to remove a project
     * All members will be notified about it in their inbox
     */
    @PostMapping("/removeProject")
    public String removeProject(@ModelAttribute("LoggedUser") User user, @RequestParam("projectName") String projectName, RedirectAttributes redirectAttributes) {
        Project project = projectService.findByName(projectName);

        if (project != null && project.getAdmins().contains(user.getUserName())) {
            List<User> users = project.getMembers().stream()
                    .map(member -> userService.findByUserName(member))
                    .collect(Collectors.toList());

            InboxMessage deletionNotification = new InboxMessage(user.getUserName() + " has removed project you take part in - " + projectName, user.getUserName(), "inboxMessage");
            for (User user1 : users) {
                user1.removeProject(projectName);
                user1.addMessage(deletionNotification);
                userService.save(user1);
            }

            projectService.delete(project);

            redirectAttributes.addFlashAttribute("successMsg", projectName + " has been removed!");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "Unable to delete " + projectName + "!");
        }

        return "redirect:/dashboard";
    }


    /**
     * @return difference between old budget an a new budget
     */
    private String moneyDifference(Project project, long before, long after) {
        if (after - before > 0)
            return "+ " + new Currency(after - before, project.getCurrencySign()).getFormattedAmount();
        else
            return "- " + new Currency(before - after, project.getCurrencySign()).getFormattedAmount();
    }
}