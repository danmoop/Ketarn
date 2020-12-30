package com.danmoop.novanode.MainApplication.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Data
@ToString
@NoArgsConstructor
@Document(collection = "projects")
public class Project {

    @Id
    private String id;

    private String name;
    private String projectKey;
    private String authorName;
    private String currencySign;

    private List<String> members;
    private List<String> admins;
    private List<Task> projectTasks;
    private List<Task> completedTasks;
    private List<ChatMessage> chatMessages;
    private List<InboxMessage> projectInbox;

    private List<ProjectItem> activeProjectItems;
    private List<ProjectItem> doneProjectItems;

    private boolean verificated;
    private long budget;

    private ProjectNotification projectNotification;

    public Project(String name, String authorName, long budget, String currencySign) {
        this.name = name;
        this.authorName = authorName;
        this.budget = budget;
        this.currencySign = currencySign;

        this.admins = new ArrayList<>();
        this.members = new ArrayList<>();
        this.projectInbox = new ArrayList<>();
        this.projectTasks = new ArrayList<>();
        this.completedTasks = new ArrayList<>();
        this.chatMessages = new ArrayList<>();
        this.activeProjectItems = new ArrayList<>();
        this.doneProjectItems = new ArrayList<>();

        this.verificated = false;
        this.projectKey = generateKey();
        this.projectNotification = null;
    }

    public Task getTaskByKey(String key) {
        return projectTasks.stream()
                .filter(task -> task.getKey().equals(key))
                .findFirst()
                .orElse(null);
    }

    public void removeTaskByKey(String key) {
        projectTasks.removeIf(task -> task.getKey().equals(key));
    }

    private String generateKey() {
        return UUID.randomUUID().toString();
    }

    public void addItem(ProjectItem projectItem, String listName) {
        switch (listName) {
            case "current":
                activeProjectItems.add(projectItem);
                break;
            case "done":
                doneProjectItems.add(projectItem);
                break;
        }
    }

    public void markAllCurrentItemsAsDone() {
        doneProjectItems.addAll(activeProjectItems);
        activeProjectItems.clear();
    }

    public void removeCard(ProjectItem projectItem) {
        activeProjectItems.remove(projectItem);
        doneProjectItems.remove(projectItem);
    }

    public ProjectItem getItemByKey(String key) {
        // we try to find an item in the first list - 'doneProjectItems'
        Optional<ProjectItem> doneItem = doneProjectItems.stream()
                .filter(item -> item.getKey().equals(key))
                .findFirst();

        // If there is no such item, then it is in the second
        Optional<ProjectItem> activeItem = activeProjectItems.stream()
                .filter(item -> item.getKey().equals(key))
                .findFirst();

        return doneItem.orElseGet(activeItem::get);
    }
}