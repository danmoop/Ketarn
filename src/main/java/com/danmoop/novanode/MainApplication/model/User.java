package com.danmoop.novanode.MainApplication.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@ToString
@NoArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String userName;
    private String name;
    private String email;
    private String password;
    private String role;
    private String note;

    private List<InboxMessage> messages;
    private List<InboxMessage> readMessages;
    private List<Task> tasks;
    private List<String> createdProjects;
    private List<String> projectsTakePartIn;
    private List<Integer> workSuccessPoints;
    private List<Task> completedTasks;

    private long registerDate;
    private boolean banned;

    public User(String userName, String name, String email, String password) {
        this.userName = userName;
        this.name = name;
        this.email = email;
        this.password = password;

        this.messages = new ArrayList<>();
        this.readMessages = new ArrayList<>();
        this.createdProjects = new ArrayList<>();
        this.projectsTakePartIn = new ArrayList<>();
        this.workSuccessPoints = new ArrayList<>();
        this.tasks = new ArrayList<>();
        this.completedTasks = new ArrayList<>();

        this.registerDate = new Date().getTime();
        this.banned = false;
        this.role = "User";
        this.note = "";
    }

    public void removeProject(String projectName) {
        projectsTakePartIn.remove(projectName);
        createdProjects.remove(projectName);
    }

    public void markMessageAsRead(InboxMessage message) {
        messages.remove(message);
        readMessages.add(message);
    }

    public boolean isRoleAdmin() {
        return role.equals("Admin");
    }

    public boolean isMember(Project project) {
        return project.getMembers().contains(userName);
    }

    public boolean isProjectAdmin(Project project) {
        return project.getAdmins().contains(userName);
    }

    public InboxMessage findMessageByMessageKey(String key) {
        return messages.stream()
                .filter(message -> message.getMessageKey().equals(key))
                .findFirst()
                .orElse(null);
    }

    public InboxMessage findReadMessageByMessageKey(String key) {
        return readMessages.stream()
                .filter(message -> message.getMessageKey().equals(key))
                .findFirst()
                .orElse(null);
    }

    public void readAllInboxMessages() {
        readMessages.addAll(messages);
        messages.clear();
    }

    public Task findTaskByKey(String key) {
        return tasks.stream()
                .filter(task -> task.getKey().equals(key))
                .findFirst()
                .orElse(null);
    }

    public double getWorkSuccessAverage() {
        int i = workSuccessPoints.stream().mapToInt(Integer::intValue).sum();

        double r = (double) i / (double) workSuccessPoints.size();

        return Math.round((r * 10) * 10) / 10.0;
    }

    public void deleteTaskByKey(String key) {
        tasks.removeIf(task -> task.getKey().equals(key));
    }
}
