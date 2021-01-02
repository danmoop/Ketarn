package com.danmoop.novanode.MainApplication.controller;

import com.danmoop.novanode.MainApplication.model.ChatMessage;
import com.danmoop.novanode.MainApplication.model.Project;
import com.danmoop.novanode.MainApplication.model.User;
import com.danmoop.novanode.MainApplication.repository.ProjectRepository;
import com.danmoop.novanode.MainApplication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class ProjectChatController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    /**
     * This request is handled when user wants to open a project chat
     *
     * @param auth        is a logged-in user object
     * @param projectName is a project name, taken from address bar (like /project/SpringFramework)
     * @return project chat page
     */
    @GetMapping("/project/{projectName}/chat")
    public String projectChat(
            @PathVariable String projectName,
            Principal auth,
            Model model) {

        Project project = projectRepository.findByName(projectName);
        User user = userRepository.findByUserName(auth.getName());

        if (project != null && project.getMembers().contains(user.getUserName())) {
            model.addAttribute("project", project);
        } else {
            return "redirect:/dashboard";
        }

        return "sections/projectChat";
    }

    /**
     * This request is handled when project member wants to send a message to chat
     * It will be saved and visible to everyone else in the chat
     *
     * @param auth        is a logged-in user object
     * @param projectName is a project name, taken from a hidden html input field
     * @return project page
     */
    @PostMapping("/sendMessageToChat")
    public String sendMessageToChat(
            @RequestParam String projectName,
            @RequestParam String message,
            Principal auth) {

        Project project = projectRepository.findByName(projectName);

        if (project != null && project.getMembers().contains(auth.getName()) && !message.equals("")) {
            ChatMessage chatMessage = new ChatMessage(message, auth.getName());
            project.getChatMessages().add(chatMessage);

            projectRepository.save(project);
        }

        return "redirect:/project/" + projectName + "/chat";
    }

    /**
     * This request is handled when project admin wants to clear the entire chat history
     * Chat messages list will be cleared and saved to database unless data is invalid
     *
     * @param auth        is a logged-in user
     * @param projectName is a project name, taken from a hidden input field, value assigned by thymeleaf
     * @return project chat page
     */

    @PostMapping("/clearProjectChat")
    public String clearProjectChar(@RequestParam String projectName, Principal auth) {
        Project project = projectRepository.findByName(projectName);

        if (project != null && project.getAdmins().contains(auth.getName())) {
            project.getChatMessages().clear();
            project.getChatMessages().add(new ChatMessage(auth.getName() + " has cleared chat history.", auth.getName()));
            projectRepository.save(project);

            return "redirect:/project/" + projectName + "/chat";
        }

        return "redirect:/dashboard";
    }
}