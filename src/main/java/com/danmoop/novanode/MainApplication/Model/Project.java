package com.danmoop.novanode.MainApplication.Model;

import com.danmoop.novanode.MainApplication.Service.Encrypt;
import org.springframework.data.annotation.Id;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Project
{
    @Id
    private String id;

    private String name;
    private String projectKey;
    private String authorName;
    private List<String> admins;
    private List<String> members;
    private ProjectNotification projectNotification;
    private long budget;
    private String currencySign;
    private List<InboxMessage> projectInbox;
    private List<Task> projectTasks;
    private List<Task> completedTasks;

    private List<ProjectItem> activeProjectItems;
    private List<ProjectItem> doneProjectItems;

    private boolean verificated;

    public Project(String name, String authorName, long budget, String currencySign) throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        this.name = name;
        this.authorName = authorName;
        this.budget = budget;
        this.currencySign = currencySign;

        this.projectKey = generateKey();
        this.admins = new ArrayList<>();
        this.members = new ArrayList<>();
        this.projectInbox = new ArrayList<>();
        this.projectTasks = new ArrayList<>();
        this.completedTasks = new ArrayList<>();

        this.activeProjectItems = new ArrayList<>();
        this.doneProjectItems = new ArrayList<>();

        this.verificated = false;

        this.projectNotification = null;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public String getProjectKey()
    {
        return projectKey;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getAuthorName()
    {
        return authorName;
    }

    public boolean isVerificated()
    {
        return verificated;
    }

    public List<Task> getCompletedTasks()
    {
        return completedTasks;
    }

    public String getCurrencySign()
    {
        return currencySign;
    }

    public void setCurrencySign(String currencySign)
    {
        this.currencySign = currencySign;
    }

    public void setVerificated(boolean verificated) {
        this.verificated = verificated;
    }

    public List<Task> getProjectTasks()
    {
        return projectTasks;
    }

    public List<ProjectItem> getActiveProjectItems()
    {
        return activeProjectItems;
    }

    public List<ProjectItem> getDoneProjectItems()
    {
        return doneProjectItems;
    }

    public void setProjectTasks(List<Task> projectTasks)
    {
        this.projectTasks = projectTasks;
    }

    public long getBudget()
    {
        return budget;
    }

    public void setBudget(long budget)
    {
        this.budget = budget;
    }

    public void setAuthorName(String authorName)
    {
        this.authorName = authorName;
    }

    public List<String> getAdmins()
    {
        return admins;
    }

    public List<InboxMessage> getProjectInbox()
    {
        return projectInbox;
    }

    public void setProjectInbox(List<InboxMessage> projectInbox) {
        this.projectInbox = projectInbox;
    }

    public void setAdmins(List<String> admins)
    {
        this.admins = admins;
    }

    public List<String> getMembers()
    {
        return members;
    }

    public void addAdmin(String admin)
    {
        admins.add(admin);
    }

    public void removeAdmin(String admin)
    {
        admins.remove(admin);
    }

    public ProjectNotification getProjectNotification()
    {
        return projectNotification;
    }

    public void setProjectNotification(ProjectNotification projectNotification)
    {
        this.projectNotification = projectNotification;
    }

    public void addMember(String member)
    {
        members.add(member);
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public void addMessage(InboxMessage message)
    {
        projectInbox.add(message);
    }

    public void addTask(Task task)
    {
        projectTasks.add(task);
    }

    public void emptyInbox()
    {
        projectInbox.clear();
    }

    public Task getTaskByKey(String key)
    {
        for(int i = 0; i < projectTasks.size(); i++)
        {
            if(projectTasks.get(i).getKey().equals(key))
               return projectTasks.get(i);
        }

        return null;
    }

    public void removeTaskByKey(String key)
    {
        for(int i = 0; i < projectTasks.size(); i++)
        {
            if(projectTasks.get(i).getKey().equals(key))
                projectTasks.remove(i);
        }
    }

    public void addCompletedTask(Task task)
    {
        completedTasks.add(task);
    }

    private String generateKey() throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        String result = "";

        String possible = "qwertyuiopasdfghjklzxcvbnm1234567890";

        for(int i = 0; i < 15; i++)
        {
            result += possible.charAt((int) (Math.random() * possible.length()));
        }

        result += new Date().getTime();

        return Encrypt.toSHA256(result);
    }

    public void addItem(ProjectItem projectItem, String listName)
    {
        switch (listName)
        {
            case "current":
                activeProjectItems.add(projectItem);
                break;
            case "done":
                doneProjectItems.add(projectItem);
                break;
        }
    }

    public void markAllCurrentItemsAsDone()
    {
        doneProjectItems.addAll(activeProjectItems);
        activeProjectItems.clear();
    }

    public void removeAllDoneItems()
    {
        doneProjectItems.clear();
    }

    public void removeCard(ProjectItem projectItem)
    {
        activeProjectItems.remove(projectItem);
        doneProjectItems.remove(projectItem);
    }

    public ProjectItem getItemByKey(String key)
    {
        for (ProjectItem doneProjectItem : doneProjectItems)
        {
            if (doneProjectItem.getKey().equals(key))
                return doneProjectItem;
        }

        for (ProjectItem currentProjectItem : activeProjectItems)
        {
            if(currentProjectItem.getKey().equals(key))
                return currentProjectItem;
        }

        return null;
    }

    @Override
    public String toString()
    {
        return "Project{" +
                "id='" + id + '\'' +
                ",\n name='" + name + '\'' +
                ",\n projectKey='" + projectKey + '\'' +
                ",\n authorName='" + authorName + '\'' +
                ",\n admins=" + admins +
                ",\n members=" + members +
                ",\n projectNotification=" + projectNotification +
                ",\n budget=" + budget +
                ",\n currencySign='" + currencySign + '\'' +
                ",\n projectInbox=" + projectInbox +
                ",\n projectTasks=" + projectTasks +
                ",\n completedTasks=" + completedTasks +
                ",\n activeProjectItems=" + activeProjectItems +
                ",\n doneProjectItems=" + doneProjectItems +
                ",\n verificated=" + verificated +
                '}';
    }
}