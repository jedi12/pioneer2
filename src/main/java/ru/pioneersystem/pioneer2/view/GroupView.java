package ru.pioneersystem.pioneer2.view;

import org.primefaces.context.RequestContext;
import ru.pioneersystem.pioneer2.model.Group;
import ru.pioneersystem.pioneer2.model.User;
import ru.pioneersystem.pioneer2.service.GroupService;
import ru.pioneersystem.pioneer2.service.RoleService;
import ru.pioneersystem.pioneer2.service.UserService;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

@ManagedBean
@ViewScoped
public class GroupView implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Group> groupList;
    private List<Group> filteredGroup;
    private Group selectedGroup;

    private boolean createFlag;
    private Group currGroup;

    private Map<String, Integer> selectRole;
    private List<String> selectUser;
    private Map<String, Integer> selectUserDefault;
    private String selectedUser;

    @ManagedProperty("#{groupService}")
    private GroupService groupService;

    @ManagedProperty("#{roleService}")
    private RoleService roleService;

    @ManagedProperty("#{userService}")
    private UserService userService;

    @PostConstruct
    public void init()  {
        refreshList();
    }

    private void refreshList() {
        try {
            currGroup = new Group();
            groupList = groupService.getGroupList();
            selectRole = roleService.getRoleMap();
            selectUserDefault = userService.getUserMap();
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.list.refresh");
        }
    }

    public void newDialog() {
        createFlag = true;
        currGroup = new Group();
        currGroup.setLinkUsers(new LinkedList<>());
        selectUser = getCurrSelectUser(currGroup);

        RequestContext.getCurrentInstance().execute("PF('editDialog').show()");
    }

    public void editDialog() {
        createFlag = false;

        if (selectedGroup == null) {
            showGrowl(FacesMessage.SEVERITY_WARN, "warn", "error.list.element.not.selected");
            return;
        }

        try {
            currGroup = groupService.getGroup(selectedGroup.getId());
            selectUser = getCurrSelectUser(currGroup);

            RequestContext.getCurrentInstance().execute("PF('editDialog').show()");
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.element.not.loaded");
        }
    }

    public void saveAction() {
        try {
            if (createFlag) {
                groupService.createGroup(currGroup);
            } else {
                groupService.updateGroup(currGroup);
            }

            refreshList();
            RequestContext.getCurrentInstance().execute("PF('editDialog').hide();");
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.not.saved");
        }
    }

    public void deleteDialog() {
        if (selectedGroup == null) {
            showGrowl(FacesMessage.SEVERITY_WARN, "warn", "error.list.element.not.selected");
            return;
        }

        RequestContext.getCurrentInstance().execute("PF('deleteDialog').show()");
    }

    public void deleteAction() {
        try {
            groupService.deleteGroup(selectedGroup.getId());
            refreshList();
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.not.deleted");
        }

        RequestContext.getCurrentInstance().execute("PF('deleteDialog').hide();");
    }

    public void addValue() {
        if (selectUser.isEmpty()) {
            return;
        }

        User user = new User();
        user.setName(selectedUser);
        Group.LinkUser linkUser = new Group.LinkUser();
        linkUser.setUser(user);
        linkUser.setUserId(selectUserDefault.get(selectedUser));
        linkUser.setParticipant(true);
        currGroup.getLinkUsers().add(linkUser);

        selectUser.remove(selectedUser);
    }

    public void removeValue(Group.LinkUser collectedList) {
        selectUser.add(collectedList.getUser().getName());
    }

    private List<String> getCurrSelectUser(Group currGroup) {
        List<String> currSelectUser = new LinkedList<>();
        currSelectUser.addAll(selectUserDefault.keySet());
        for (Group.LinkUser linkUser: currGroup.getLinkUsers()) {
            currSelectUser.remove(linkUser.getUser().getName());
        }
        return currSelectUser;
    }

    private void showGrowl(FacesMessage.Severity severity, String shortMessage, String longMessage) {
        ResourceBundle bundle = ResourceBundle.getBundle("text", FacesContext.getCurrentInstance().getViewRoot().getLocale());
        FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(
                severity, bundle.getString(shortMessage), bundle.getString(longMessage)));
    }

    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }

    public void setRoleService(RoleService roleService) {
        this.roleService = roleService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public List<Group> getGroupList() {
        return groupList;
    }

    public boolean isCreateFlag() {
        return createFlag;
    }

    public List<Group> getFilteredGroup() {
        return filteredGroup;
    }

    public void setFilteredGroup(List<Group> filteredGroup) {
        this.filteredGroup = filteredGroup;
    }

    public Group getSelectedGroup() {
        return selectedGroup;
    }

    public void setSelectedGroup(Group selectedGroup) {
        this.selectedGroup = selectedGroup;
    }

    public Group getCurrGroup() {
        return currGroup;
    }

    public void setCurrGroup(Group currGroup) {
        this.currGroup = currGroup;
    }

    public Map<String, Integer> getSelectRole() {
        return selectRole;
    }

    public List<String> getSelectUser() {
        return selectUser;
    }

    public String getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(String selectedUser) {
        this.selectedUser = selectedUser;
    }
}