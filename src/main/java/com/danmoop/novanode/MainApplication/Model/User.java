package com.danmoop.novanode.MainApplication.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "users")
public class User
{
    @Id
    private String id;

    private String userName;
    private String name;
    private String email;
    private String password;
    private String role;
    private String note;
    private long registerDate;
    private boolean hasBoughtSubs;
    private boolean banned;
    private List<InboxMessage> messages;
    private List<InboxMessage> readMessages;
    private List<Task> tasks;

    private List<String> createdProjects;
    private List<String> projectsTakePartIn;
    private List<Integer> workSuccessPoints;

    private List<Task> completedTasks;

    public User() {}

    public User(String userName, String name, String email, String password)
    {
        this.userName = userName;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = "User";

        this.note = "";

        this.messages = new ArrayList<>();
        this.readMessages = new ArrayList<>();
        this.createdProjects = new ArrayList<>();
        this.projectsTakePartIn = new ArrayList<>();
        this.workSuccessPoints = new ArrayList<>();
        this.tasks = new ArrayList<>();
        this.completedTasks = new ArrayList<>();
        this.registerDate = new Date().getTime();
        this.hasBoughtSubs = true;
        this.banned = false;
    }

    public List<InboxMessage> getMessages()
    {
        return messages;
    }

    public String getRole()
    {
        return role;
    }

    public List<String> getCreatedProjects()
    {
        return createdProjects;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public List<String> getProjectsTakePartIn()
    {
        return projectsTakePartIn;
    }

    public List<Task> getTasks()
    {
        return tasks;
    }

    public String getUserName()
    {
        return userName;
    }

    public String getName()
    {
        return name;
    }

    public List<Task> getCompletedTasks()
    {
        return completedTasks;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isBanned()
    {
        return banned;
    }

    public List<Integer> getWorkSuccessPoints()
    {
        return workSuccessPoints;
    }

    public void setBanned(boolean banned)
    {
        this.banned = banned;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public boolean isHasBoughtSubs()
    {
        return hasBoughtSubs;
    }

    public void setHasBoughtSubs(boolean hasBoughtSubs)
    {
        this.hasBoughtSubs = hasBoughtSubs;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public long getRegisterDate()
    {
        return registerDate;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getNote()
    {
        return note;
    }

    public void setNote(String note)
    {
        this.note = note;
    }

    public List<InboxMessage> getReadMessages()
    {
        return readMessages;
    }

    public void addMessage(InboxMessage message)
    {
        messages.add(message);
    }

    public void removeMessage(InboxMessage message)
    {
        readMessages.remove(message);
    }

    public void removeMessageFromInbox(InboxMessage message)
    {
        messages.remove(message);
    }

    public void addProject(String project)
    {
        createdProjects.add(project);
    }

    public void addProjectTakingPartIn(String project)
    {
        projectsTakePartIn.add(project);
    }

    public void markMessageAsRead(InboxMessage message)
    {
        messages.remove(message);
        readMessages.add(message);
    }

    public boolean isAdmin(Project project)
    {
        for (int i = 0; i < project.getAdmins().size(); i++)
        {
            if(userName.equals(project.getAdmins().get(i)))
                return true;
        }

        return false;
    }

    public boolean isMember(Project project)
    {

        for (int i = 0; i < project.getMembers().size(); i++)
        {
            if(userName.equals(project.getMembers().get(i)))
                return true;
        }

        return false;
    }

    public InboxMessage findMessageByMessageKey(String key)
    {
        for (int i = 0; i < messages.size(); i++)
        {
            if(messages.get(i).getMessageKey().equals(key))
                return messages.get(i);
        }

        return null;
    }

    public InboxMessage findReadMessageByMessageKey(String key)
    {
        for (int i = 0; i < readMessages.size(); i++)
        {
            if(readMessages.get(i).getMessageKey().equals(key))
                return readMessages.get(i);
        }

        return null;
    }

    public void addTask(Task task)
    {
        tasks.add(task);
    }

    public void readAllInboxMessages()
    {
        readMessages.addAll(messages);

        messages.clear();
    }

    public void emptyArchive()
    {
        readMessages.clear();
    }

    public boolean isAdmin()
    {
        if(getRole().equals("Admin"))
            return true;

        return false;
    }

    public Task findTaskByKey(String key)
    {
        for (Task task : tasks) {
            if (task.getKey().equals(key))
                return task;
        }

        return null;
    }

    public double getWorkSuccessAverage()
    {
        int i = 0;

        for (int point: workSuccessPoints)
        {
            i += point;
        }

        double r = (double) i / (double) workSuccessPoints.size();

        System.out.println(r);

        double result = Math.round((r * 10) * 10) / 10.0;

        return result;

    }

    public void addWorkSuccessPoint(int point)
    {
        workSuccessPoints.add(point);
    }

    public void deleteTaskByKey(String key)
    {
        for(int i = 0; i < tasks.size(); i++)
        {
            if(tasks.get(i).getKey().equals(key))
                tasks.remove(i);
        }
    }

    public void addCompletedTask(Task task)
    {
        completedTasks.add(task);
    }
}
