package com.danmoop.novanode.MainApplication.controller;

import com.danmoop.novanode.MainApplication.misc.Currency;
import com.danmoop.novanode.MainApplication.model.*;
import com.danmoop.novanode.MainApplication.repository.ProjectRepository;
import com.danmoop.novanode.MainApplication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class ProjectController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * This request is handled when user redirects to project creation page
     *
     * @return project creation html page
     */
    @GetMapping("/createProject")
    public String createProject(Principal auth) {
        if (auth == null) {
            return "redirect:/";
        }

        User user = userRepository.findByUserName(auth.getName());

        if (user.isBanned()) {
            return userIsBanned();
        }

        return "sections/createProject";
    }

    /**
     * This request is handled when user fills in project info and submits it
     * Project is created and saved to database
     *
     * @param auth          is a logged-in user object
     * @param projectBudget is a project budget. It is taken from html textfield
     * @param projectName   is a project name, taken from html textfield
     * @param currencySign  is a budget currency sign (USD, EUR...), taken from html select field
     * @return dashboard page with new project and other user's information
     */
    @PostMapping("/createProject")
    public String createProjectPOST(Principal auth, @RequestParam String projectName, @RequestParam String currencySign, @RequestParam long projectBudget) {
        if (projectRepository.findByName(projectName) == null && auth != null) {
            User user = userRepository.findByUserName(auth.getName());

            if (user.isBanned()) {
                return userIsBanned();
            }

            Project project = new Project(projectName, user.getUserName(), projectBudget, currencySign);

            user.getCreatedProjects().add(projectName);
            user.getProjectsTakePartIn().add(projectName);

            project.getAdmins().add(user.getUserName());
            project.getMembers().add(user.getUserName());

            userRepository.save(user);
            projectRepository.save(project);

            return "redirect:/project/" + projectName;
        }

        return "redirect:/";
    }

    /**
     * This request is handled when project admin wants to set a notification for other members
     *
     * @param auth        is a logged-in user object
     * @param projectName is a project name, taken from html text field
     * @param messageText is a notification text message
     * @return project page with new notification
     * @see ProjectNotification
     */
    @PostMapping("/setProjectNotification")
    public String notificationSubmitted(@RequestParam String messageText, @RequestParam String projectName, Principal auth) {
        if (auth == null) {
            return "redirect:/";
        }

        User user = userRepository.findByUserName(auth.getName());

        if (user.isBanned()) {
            return userIsBanned();
        }

        Project projectDB = projectRepository.findByName(projectName);

        if (user.isProjectAdmin(projectDB)) {
            ProjectNotification projectNotification = new ProjectNotification(auth.getName(), messageText);

            projectDB.setProjectNotification(projectNotification);
            projectRepository.save(projectDB);

            return "redirect:/project/" + projectName;
        }

        return "redirect:/dashboard";
    }

    /**
     * This request is handled when project admin wants to set a new project budget
     *
     * @param auth              is a logged-in user object
     * @param projectName       is a project name, taken from html textfield
     * @param budget            is a new project budget
     * @param budgetChangeCause states why budget is changed. It will be seen to everybody
     * @return project page with new budget
     * @see InboxMessage
     */
    @PostMapping("/setProjectBudget")
    public String setBudget(@RequestParam("projectBudget") long budget, @RequestParam String projectName, @RequestParam String budgetChangeCause, Principal auth) {
        if (auth == null) {
            return "redirect:/";
        }

        User user = userRepository.findByUserName(auth.getName());

        if (user.isBanned()) {
            return userIsBanned();
        }

        Project projectDB = projectRepository.findByName(projectName);

        if (user.isProjectAdmin(projectDB)) {
            String difference = moneyDifference(projectDB, projectDB.getBudget(), budget);

            InboxMessage notification = new InboxMessage(user.getUserName() + " has changed project budget from " + new Currency(projectDB.getBudget(), projectDB.getCurrencySign()).getFormattedAmount() + " to " + new Currency(budget, projectDB.getCurrencySign()).getFormattedAmount() + "\n\nReason: " + budgetChangeCause + "\n\nSummary: (" + difference + ")", user.getUserName(), "inboxMessage");

            projectDB.getProjectInbox().add(notification);
            projectDB.setBudget(budget);
            projectRepository.save(projectDB);

            return "redirect:/project/" + projectName;
        }

        return "redirect:/dashboard";
    }

    /**
     * This request is handled when project admin wants to clear project inbox
     * Actually it will be cleared, but the only 1 message will remain - saying who cleared it
     *
     * @param auth        is a logged-in user object
     * @param projectName is a project name, taken from html text field
     * @return project page with an empty inbox
     */
    @PostMapping("/deleteAllInboxMessages")
    public String deleteAllInbox(Principal auth, @RequestParam String projectName, RedirectAttributes redirectAttributes) {
        if (auth == null) {
            return "redirect:/";
        }

        User user = userRepository.findByUserName(auth.getName());

        if (user.isBanned()) {
            return userIsBanned();
        }

        Project projectDB = projectRepository.findByName(projectName);

        if (projectDB != null && user.isProjectAdmin(projectDB)) {
            projectDB.getProjectInbox().clear();
            projectDB.getProjectInbox().add(new InboxMessage(user.getUserName() + " has cleared project inbox.", user.getUserName(), "inboxMessage"));
            projectRepository.save(projectDB);

            redirectAttributes.addFlashAttribute("successMsg", "Project inbox cleared");

            return "redirect:/project/" + projectName;
        }

        return "redirect:/dashboard";
    }

    /**
     * This request is handled when user wants to open a project page
     * If a user is a member of a project -> let them in, otherwise show a corresponding message
     *
     * @param auth        is a logged-in user object
     * @param projectName is a project name, taken from address bar (like /project/SpringFramework)
     * @return project page if user is a member of the project, otherwise redirect to not-a-member page
     * @see Currency
     */
    @GetMapping("/project/{projectName}")
    public String projectPage(@PathVariable String projectName, Model model, Principal auth) {
        if (auth == null) {
            return "redirect:/";
        }

        Project projectDB = projectRepository.findByName(projectName);

        User user = userRepository.findByUserName(auth.getName());

        if (user.isBanned()) {
            return userIsBanned();
        }

        if (projectDB != null) {
            if (user.isMember(projectDB)) {
                model.addAttribute("project", projectRepository.findByName(projectName));
                model.addAttribute("LoggedUser", user);

                Currency budget = new Currency(projectDB.getBudget(), projectDB.getCurrencySign());
                model.addAttribute("budget", budget.getFormattedAmount());

                return "sections/projectDashboard";
            } else {
                model.addAttribute("projectName", projectName);
                model.addAttribute("LoggedUser", user);
                return "handlingPages/notamember";
            }
        }

        return "error/404";
    }

    /**
     * @param projectName is a project name, taken from address bar (like /project/SpringFramework)
     * @return dashboard page
     * @see InboxMessage  for explanation of InboxMessage type
     * This request is handled when user opened a project page and saw that they are not a part of project's team
     * They can press a button -> 'send request to join' and all project admins will get that request
     */
    @PostMapping("/sendARequest")
    public String joinRequest(@RequestParam String projectName, RedirectAttributes redirectAttributes, Principal auth) {
        if (auth == null) {
            return "redirect:/";
        }

        User user = userRepository.findByUserName(auth.getName());

        if (user.isBanned()) {
            return userIsBanned();
        }

        Project projectDB = projectRepository.findByName(projectName);
        InboxMessage message = new InboxMessage(auth.getName() + " wants to join " + projectName + " project. \nAccept this request or reject.", auth.getName(), "inboxRequest");
        message.setDetails(projectName);

        projectDB.getAdmins().stream()
                .map(adminName -> userRepository.findByUserName(adminName))
                .forEach(admin -> {
                    admin.getMessages().add(message);
                    userRepository.save(admin);
                });

        redirectAttributes.addFlashAttribute("successMsg", "Request sent successfully to " + projectName + " project!");

        return "redirect:/dashboard";
    }

    /**
     * This request is handled when project admin wants to invite specific user to the project
     *
     * @param auth        is a logged-in user object
     * @param projectName is a project name, taken from a hidden html input field
     * @param memberName  is the user who gets the invitation
     * @return project page
     * @see InboxMessage
     */
    @PostMapping("/inviteMemberToProject")
    public String inviteMemberToProject(@RequestParam String projectName, @RequestParam String memberName, RedirectAttributes redirectAttributes, Principal auth) {
        if (auth == null) {
            return "redirect:/";
        }

        User user = userRepository.findByUserName(auth.getName());

        if (user.isBanned()) {
            return userIsBanned();
        }

        InboxMessage message = new InboxMessage(auth.getName() + " has invited you to join " + projectName + " project. Accept this invite or reject.", auth.getName(), "inboxRequestToMember");
        User userRecipient = userRepository.findByUserName(memberName);

        if (userRecipient != null) {
            message.setDetails(projectName);
            userRecipient.getMessages().add(message);
            userRepository.save(userRecipient);

            redirectAttributes.addFlashAttribute("successMsg", memberName + " has been invited!");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "There is no user with such username: " + memberName);
        }

        return "redirect:/project/" + projectName;
    }

    /**
     * @param auth        is a logged-in user object
     * @param projectName is a project name, taken from a hidden html input field
     * @param memberName  is a name of the user who becomes an admin
     * @return project page
     * @see InboxMessage
     * This request is handled when project admin wants to make another user admin
     */
    @PostMapping("/setMemberAsAdmin")
    public String setAsAdmin(@RequestParam String projectName, @RequestParam String memberName, Principal auth, RedirectAttributes redirectAttributes) {
        if (auth == null) {
            return "redirect:/";
        }

        User user = userRepository.findByUserName(auth.getName());

        if (user.isBanned()) {
            return userIsBanned();
        }

        Project projectDB = projectRepository.findByName(projectName);
        User member = userRepository.findByUserName(memberName);

        if (user.isProjectAdmin(projectDB) && !projectDB.getAdmins().contains(memberName) && member != null) {
            InboxMessage message = new InboxMessage(user.getUserName() + " has set " + memberName + " as project admin", user.getUserName(), "inboxMessage");

            projectDB.getProjectInbox().add(message);
            projectDB.getAdmins().add(memberName);
            projectRepository.save(projectDB);

            InboxMessage userMessage = new InboxMessage(user.getUserName() + " has set you as " + projectName + "'s admin", user.getUserName(), "inboxMessage");
            member.getMessages().add(userMessage);
            userRepository.save(member);

            redirectAttributes.addFlashAttribute("successMsg", memberName + " has been set as " + projectName + "'s admin");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "An error has occurred");
        }

        return "redirect:/project/" + projectName;
    }

    /**
     * @param auth         is a logged-in user object
     * @param projectName  is a project name, taken from a hidden html input field
     * @param currentAdmin is a name of the user who no longer will be an admin
     * @return project page
     * @see InboxMessage
     * This request is handled when project admin wants to take another user's admin right
     * That user will no longer be an admin
     */
    @PostMapping("/unAdmin")
    public String unAdminUser(@RequestParam String projectName, @RequestParam String currentAdmin, Principal auth, RedirectAttributes redirectAttributes) {
        if (auth == null) {
            return "redirect:/";
        }

        User user = userRepository.findByUserName(auth.getName());

        if (user.isBanned()) {
            return userIsBanned();
        }

        Project projectDB = projectRepository.findByName(projectName);

        if (projectDB != null && projectDB.getAdmins().contains(user.getUserName()) && !user.getUserName().equals(currentAdmin)) {
            projectDB.getAdmins().remove(currentAdmin);

            InboxMessage message = new InboxMessage(user.getUserName() + " has taken admin right from " + currentAdmin, user.getUserName(), "inboxMessage");
            projectDB.getProjectInbox().add(message);
            projectRepository.save(projectDB);

            redirectAttributes.addFlashAttribute("successMsg", currentAdmin + " is not an admin anymore!");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "You can not un-admin yourself");
        }

        return "redirect:/project/" + projectName;
    }

    /**
     * @param auth          is a logged-in user object
     * @param projectName   is a project name, taken from a hidden html input field
     * @param directoryName is a directory name where that item belongs
     * @return project page
     * @see ProjectItem
     * This request is handled when project admin wants to add another project item to project
     * This item will be added to project to it's directory and saved
     */
    @PostMapping("/addNewCard")
    public String newCard(@RequestParam String projectName, @RequestParam String itemText, @RequestParam String directoryName, Principal auth) {
        if (auth == null) {
            return "redirect:/";
        }

        User user = userRepository.findByUserName(auth.getName());

        if (user.isBanned()) {
            return userIsBanned();
        }

        ProjectItem projectItem = new ProjectItem(itemText, projectName);
        Project project = projectRepository.findByName(projectName);

        if (project.getAdmins().contains(user.getUserName())) {
            project.addItem(projectItem, directoryName);
            projectRepository.save(project);
        }

        return "redirect:/project/" + projectName;
    }

    /**
     * @param auth        is a logged-in user object
     * @param projectName is a project name, taken from a hidden html input field
     * @return project page
     * @see ProjectItem
     * This request is handled when project admin wants to remove a project item
     * It will be removed
     */
    @PostMapping("/removeItem")
    public String removeCard(@RequestParam String projectName, @RequestParam String cardKey, Principal auth) {
        if (auth == null) {
            return "redirect:/";
        }

        User user = userRepository.findByUserName(auth.getName());

        if (user.isBanned()) {
            return userIsBanned();
        }

        Project project = projectRepository.findByName(projectName);

        if (project != null && project.getAdmins().contains(user.getUserName())) {
            ProjectItem projectItem = project.getItemByKey(cardKey);
            project.removeCard(projectItem);

            projectRepository.save(project);
        }

        return "redirect:/project/" + projectName;
    }

    /**
     * @param auth        is a logged-in user object
     * @param projectName is a project name, taken from a hidden html input field
     * @return project page
     * @see ProjectItem
     * This request is handled when project admin wants to mark the item as done
     * The item will be moved to 'done' category
     */
    @PostMapping("/markItemAsDone")
    public String markItemAsDone(@RequestParam String projectName, @RequestParam String itemKey, Principal auth) {
        if (auth == null) {
            return "redirect:/";
        }

        User user = userRepository.findByUserName(auth.getName());

        if (user.isBanned()) {
            return userIsBanned();
        }

        Project project = projectRepository.findByName(projectName);

        if (project != null && project.getAdmins().contains(user.getUserName())) {
            ProjectItem projectItem = project.getItemByKey(itemKey);

            project.removeCard(projectItem);
            project.addItem(projectItem, "done");
            projectRepository.save(project);
        }

        return "redirect:/project/" + projectName;
    }

    /**
     * This request is handled when project admin wants to mark all current items as done
     * All of them will be moved to 'done' category
     *
     * @param auth        is a logged-in user object
     * @param projectName is a project name, taken from a hidden html input field
     * @return project page
     */
    @PostMapping("/currentItemsAllDone")
    public String currentItemsAllDone(@RequestParam String projectName, Principal auth) {
        if (auth == null) {
            return "redirect:/";
        }

        User user = userRepository.findByUserName(auth.getName());

        if (user.isBanned()) {
            return userIsBanned();
        }

        Project project = projectRepository.findByName(projectName);

        if (project != null && project.getAdmins().contains(auth.getName())) {
            project.markAllCurrentItemsAsDone();
            projectRepository.save(project);
        }

        return "redirect:/project/" + projectName;
    }

    /**
     * This request is handled when project admin wants to mark all done items as done
     * All of them will be removed
     *
     * @param auth        is a logged-in user object
     * @param projectName is a project name, taken from a hidden html input field
     * @return project page
     */
    @PostMapping("/removeDoneItems")
    public String removeDoneItems(@RequestParam String projectName, Principal auth) {
        if (auth == null) {
            return "redirect:/";
        }

        User user = userRepository.findByUserName(auth.getName());

        if (user.isBanned()) {
            return userIsBanned();
        }

        Project project = projectRepository.findByName(projectName);

        if (project != null && project.getAdmins().contains(auth.getName())) {
            project.getDoneProjectItems().clear();
            projectRepository.save(project);
        }

        return "redirect:/project/" + projectName;
    }

    /**
     * This request is handled when project admin wants to remove a project
     * All members will be notified about it in their inbox
     *
     * @param auth        is a logged-in user object
     * @param projectName is a project name, taken from a hidden html input field
     * @return dashboard page
     */
    @PostMapping("/removeProject")
    public String removeProject(@RequestParam String projectName, Principal auth, RedirectAttributes redirectAttributes) {
        if (auth == null) {
            return "redirect:/";
        }

        User user = userRepository.findByUserName(auth.getName());

        if (user.isBanned()) {
            return userIsBanned();
        }

        Project project = projectRepository.findByName(projectName);

        if (project != null && project.getAdmins().contains(auth.getName())) {
            InboxMessage deletionNotification = new InboxMessage(auth.getName() + " has removed project you take part in - " + projectName, auth.getName(), "inboxMessage");

            project.getMembers().stream()
                    .map(member -> userRepository.findByUserName(member))
                    .forEach(memberUser -> {
                        memberUser.removeProject(projectName);
                        memberUser.getMessages().add(deletionNotification);
                        userRepository.save(memberUser);
                    });

            projectRepository.delete(project);

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

    private String userIsBanned() {
        SecurityContextHolder.clearContext();
        return "handlingPages/youarebanned";
    }
}