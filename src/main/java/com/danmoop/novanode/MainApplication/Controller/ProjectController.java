package com.danmoop.novanode.MainApplication.Controller;

import com.danmoop.novanode.MainApplication.Model.*;
import com.danmoop.novanode.MainApplication.Service.ProjectService;
import com.danmoop.novanode.MainApplication.Service.UserService;
import com.danmoop.novanode.MainApplication.misc.Currency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Document(collection = "projects")

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
    public String createProject() {
        return "sections/createProject";
    }


    /**
     * This request is handled when user fills in project info and submits it
     * Project is created and saved to database
     *
     * @param principal     is a logged-in user object
     * @param projectBudget is a project budget. It is taken from html textfield
     * @param projectName   is a project name, taken from html textfield
     * @param currencySign  is a budget currency sign (USD, EUR...), taken from html select field
     * @return dashboard page with new project and other user's information
     */
    @PostMapping("/createProject")
    public String createProjectPOST(
            Principal principal,
            @RequestParam("projectName") String projectName,
            @RequestParam("projectBudget") long projectBudget,
            @RequestParam("currencySign") String currencySign
    ) {
        if (projectService.findByName(projectName) == null && principal != null) {
            User user = userService.findByUserName(principal.getName());

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
     * This request is handled when project admin wants to set a notification for other members
     *
     * @param principal   is a logged-in user object
     * @param projectName is a project name, taken from html textfield
     * @param text        is a notification text message
     * @return project page with new notification
     * @see ProjectNotification
     */
    @PostMapping("/setProjectNotification")
    public String notificationSubmitted(
            Principal principal,
            @RequestParam("messageText") String text,
            @RequestParam("projectName") String projectName) {
        User userDB = userService.findByUserName(principal.getName());
        Project projectDB = projectService.findByName(projectName);

        if (userDB.isProjectAdmin(projectDB)) {
            ProjectNotification projectNotification = new ProjectNotification(principal.getName(), text);

            projectDB.setProjectNotification(projectNotification);
            projectService.save(projectDB);

            return "redirect:/project/" + projectName;
        }

        return "redirect:/dashboard";
    }


    /**
     * This request is handled when project admin wants to set a new project budget
     *
     * @param principal   is a logged-in user object
     * @param projectName is a project name, taken from html textfield
     * @param budget      is a new project budget
     * @param reason      states why budget is changed. It will be seen to everybody
     * @return project page with new budget
     * @see InboxMessage
     */
    @PostMapping("/setProjectBudget")
    public String setBudget(
            Principal principal,
            @RequestParam("projectBudget") long budget,
            @RequestParam("projectName") String projectName,
            @RequestParam("budgetChangeCause") String reason) {

        User userDB = userService.findByUserName(principal.getName());
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
            } else
                redirectAttributes.addFlashAttribute("errorMsg", "You can't give a task to yourself. Only other member can do that");
        }
        return "redirect:/project/" + projectName;
    }

    /**
     * @param principal   is a logged-in user object
     * @param projectName is a project name, taken from html textfield
     * @return project page with an empty inbox
     * @see InboxMessage
     * <p>
     * This request is handled when project admin wants to clear project inbox
     * Actually it will be cleared, but the only 1 message will remain - saying who cleared it
     */
    @PostMapping("/deleteAllInboxMessages")
    public String deleteAllInbox(Principal principal, @RequestParam("projectName") String projectName, RedirectAttributes redirectAttributes) {
        Project projectDB = projectService.findByName(projectName);
        User user = userService.findByUserName(principal.getName());

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
     * This request is handled when user wants to open a project page
     *
     * @param principal   is a logged-in user object
     * @param projectName is a project name, taken from address bar (like /project/SpringFramework)
     * @return project page if user is a member of the project, otherwise redirect to not-a-member page
     * @see Currency
     */
    @GetMapping("/project/{projectName}")
    public String projectPage(@PathVariable String projectName, Model model, Principal principal) {
        Project projectDB = projectService.findByName(projectName);
        User userDB = userService.findByUserName(principal.getName());

        if (projectDB != null && userDB != null) {
            if (userDB.isMember(projectDB)) {
                model.addAttribute("project", projectService.findByName(projectName));
                model.addAttribute("LoggedUser", userDB);

                Currency budget = new Currency(projectDB.getBudget(), projectDB.getCurrencySign());
                model.addAttribute("budget", budget.getFormattedAmount());

                return "sections/projectDashboard";
            } else {
                model.addAttribute("projectName", projectName);
                model.addAttribute("LoggedUser", userDB);
                return "handlingPages/notamember";
            }
        }

        return "error/404";
    }

    /**
     * @param projectName is a project name, taken from address bar (like /project/SpringFramework)
     * @param userName    is a name of the user who wants to join the project
     * @return dashboard page
     * @see InboxMessage  for explanation of InboxMessage type
     * <p>
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
     * This request is handled when project admin wants to invite specific user to the project
     *
     * @param principal   is a logged-in user object
     * @param projectName is a project name, taken from a hidden html input field
     * @param recipient   is the user who gets the invitation
     * @return project page
     * @see InboxMessage
     */
    @PostMapping("/inviteMemberToProject")
    public String inviteMemberToProject(
            Principal principal,
            @RequestParam("projectName") String projectName,
            @RequestParam("memberName") String recipient,
            RedirectAttributes redirectAttributes) {

        InboxMessage message = new InboxMessage(principal.getName() + " has invited you to join " + projectName + " project. Accept this invite or reject.", principal.getName(), "inboxRequestToMember");
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
     * @param principal   is a logged-in user object
     * @param projectName is a project name, taken from a hidden html input field
     * @param memberName  is a name of the user who becomes an admin
     * @return project page
     * @see InboxMessage
     * This request is handled when project admin wants to make another user admin
     */
    @PostMapping("/setMemberAsAdmin")
    public String setAsAdmin(
            @RequestParam("projectName") String projectName,
            @RequestParam("memberName") String memberName,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        Project projectDB = projectService.findByName(projectName);
        User member = userService.findByUserName(memberName);
        User user = userService.findByUserName(principal.getName());

        if (user.isProjectAdmin(projectDB) && !projectDB.getAdmins().contains(memberName) && member != null) {
            InboxMessage message = new InboxMessage(user.getUserName() + " has set " + memberName + " as project admin", user.getUserName(), "inboxMessage");

            projectDB.addMessage(message);
            projectDB.addAdmin(memberName);
            projectService.save(projectDB);

            InboxMessage userMessage = new InboxMessage(user.getUserName() + " has set you as " + projectName + "'s admin", user.getUserName(), "inboxMessage");
            member.addMessage(userMessage);
            userService.save(member);

            redirectAttributes.addFlashAttribute("successMsg", memberName + " has been set as " + projectName + "'s admin");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "An error has occurred");
        }

        return "redirect:/project/" + projectName;
    }


    /**
     * @param principal    is a logged-in user object
     * @param projectName  is a project name, taken from a hidden html input field
     * @param currentAdmin is a name of the user who no longer will be an admin
     * @return project page
     * @see InboxMessage
     * <p>
     * This request is handled when project admin wants to take another user's admin right
     * That user will no longer be an admin
     */
    @PostMapping("/unAdmin")
    public String unAdminUser(
            @RequestParam("projectName") String projectName,
            @RequestParam("currentAdmin") String currentAdmin,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        Project projectDB = projectService.findByName(projectName);
        User user = userService.findByUserName(principal.getName());

        if (projectDB != null && projectDB.getAdmins().contains(user.getUserName()) && !user.getUserName().equals(currentAdmin)) {
            projectDB.removeAdmin(currentAdmin);

            InboxMessage message = new InboxMessage(user.getUserName() + " has taken admin right from " + currentAdmin, user.getUserName(), "inboxMessage");
            projectDB.addMessage(message);
            projectService.save(projectDB);

            redirectAttributes.addFlashAttribute("successMsg", currentAdmin + " is not an admin anymore!");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "You can not un-admin yourself");
        }

        return "redirect:/project/" + projectName;
    }


    /**
     * @param principal     is a logged-in user object
     * @param projectName   is a project name, taken from a hidden html input field
     * @param directoryName is a directory name where that item belongs
     * @return project page
     * @see ProjectItem
     * <p>
     * This request is handled when project admin wants to add another project item to project
     * This item will be added to project to it's directory and saved
     */
    @PostMapping("/addNewCard")
    public String newCard(
            @RequestParam("projectName") String projectName,
            @RequestParam("itemText") String itemText,
            @RequestParam("directoryName") String directoryName,
            Principal principal) {

        ProjectItem projectItem = new ProjectItem(itemText, projectName);
        Project project = projectService.findByName(projectName);
        User user = userService.findByUserName(principal.getName());

        if (project.getAdmins().contains(user.getUserName())) {
            project.addItem(projectItem, directoryName);
            projectService.save(project);
        }

        return "redirect:/project/" + projectName;
    }

    /**
     * @param principal   is a logged-in user object
     * @param projectName is a project name, taken from a hidden html input field
     * @return project page
     * @see ProjectItem
     * <p>
     * This request is handled when project admin wants to remove a project item
     * It will be removed
     */
    @PostMapping("/removeItem")
    public String removeCard(
            @RequestParam("projectName") String projectName,
            @RequestParam("itemKey") String cardKey,
            Principal principal
    ) {
        Project project = projectService.findByName(projectName);
        User user = userService.findByUserName(principal.getName());

        if (project != null && project.getAdmins().contains(user.getUserName())) {
            ProjectItem projectItem = project.getItemByKey(cardKey);

            project.removeCard(projectItem);

            projectService.save(project);
        }

        return "redirect:/project/" + projectName;
    }

    /**
     * @param principal   is a logged-in user object
     * @param projectName is a project name, taken from a hidden html input field
     * @return project page
     * @see ProjectItem
     * <p>
     * This request is handled when project admin wants to mark the item as done
     * The item will be moved to 'done' category
     */
    @PostMapping("/markItemAsDone")
    public String markItemAsDone(
            @RequestParam("projectName") String projectName,
            @RequestParam("itemKey") String itemKey,
            Principal principal
    ) {
        Project project = projectService.findByName(projectName);
        User user = userService.findByUserName(principal.getName());

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
     * @param principal   is a logged-in user object
     * @param projectName is a project name, taken from a hidden html input field
     * @return project page
     */
    @PostMapping("/currentItemsAllDone")
    public String currentItemsAllDone(@RequestParam("projectName") String projectName, Principal principal) {
        Project project = projectService.findByName(projectName);

        if (project != null && project.getAdmins().contains(principal.getName())) {
            project.markAllCurrentItemsAsDone();
            projectService.save(project);
        }

        return "redirect:/project/" + projectName;
    }


    /**
     * This request is handled when project admin wants to mark all done items as done
     * All of them will be removed
     *
     * @param principal   is a logged-in user object
     * @param projectName is a project name, taken from a hidden html input field
     * @return project page
     */
    @PostMapping("/removeDoneItems")
    public String removeDoneItems(@RequestParam("projectName") String projectName, Principal principal) {
        Project project = projectService.findByName(projectName);

        if (project != null && project.getAdmins().contains(principal.getName())) {
            project.removeAllDoneItems();
            projectService.save(project);
        }

        return "redirect:/project/" + projectName;
    }

    /**
     * This request is handled when project admin wants to remove a project
     * All members will be notified about it in their inbox
     *
     * @param principal   is a logged-in user object
     * @param projectName is a project name, taken from a hidden html input field
     * @return dashboard page
     */
    @PostMapping("/removeProject")
    public String removeProject(Principal principal, @RequestParam("projectName") String projectName, RedirectAttributes redirectAttributes) {
        Project project = projectService.findByName(projectName);

        if (project != null && project.getAdmins().contains(principal.getName())) {
            List<User> users = project.getMembers().stream()
                    .map(member -> userService.findByUserName(member))
                    .collect(Collectors.toList());

            InboxMessage deletionNotification = new InboxMessage(principal.getName() + " has removed project you take part in - " + projectName, principal.getName(), "inboxMessage");

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
        if (after - before > 0) {
            return "+ " + new Currency(after - before, project.getCurrencySign()).getFormattedAmount();
        } else {
            return "- " + new Currency(before - after, project.getCurrencySign()).getFormattedAmount();
        }
    }
}