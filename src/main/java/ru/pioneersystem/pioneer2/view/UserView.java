package ru.pioneersystem.pioneer2.view;

import org.primefaces.context.RequestContext;
import ru.pioneersystem.pioneer2.model.User;
import ru.pioneersystem.pioneer2.service.GroupService;
import ru.pioneersystem.pioneer2.service.UserService;
import ru.pioneersystem.pioneer2.service.exception.RestrictionException;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

@ManagedBean
@ViewScoped
public class UserView implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<User> userList;
    private List<User> filteredUserList;
    private User selectedUser;

    private User currUser;

    private List<String> selectGroup;
    private Map<String, Integer> selectGroupDefault;
    private String selectedGroup;

    private String newPass;

    private ResourceBundle bundle;

    @ManagedProperty("#{userService}")
    private UserService userService;

    @ManagedProperty("#{groupService}")
    private GroupService groupService;

    @PostConstruct
    public void init() {
        bundle = ResourceBundle.getBundle("text", FacesContext.getCurrentInstance().getViewRoot().getLocale());
        currUser = userService.getNewUser();
    }

    public void refreshList() {
        try {
            userList = userService.getUserList();
            selectGroupDefault = groupService.getGroupMap();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void newDialog() {
        currUser = userService.getNewUser();
        selectGroup = getCurrSelectGroup(currUser);

        RequestContext.getCurrentInstance().execute("PF('editDialog').show()");
    }

    public void editDialog() {
        try {
            currUser = userService.getUser(selectedUser);
            selectGroup = getCurrSelectGroup(currUser);

            RequestContext.getCurrentInstance().execute("PF('editDialog').show()");
        } catch (RestrictionException e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), e.getMessage()));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void saveAction() {
        try {
            userService.saveUser(currUser);

            refreshList();
            RequestContext.getCurrentInstance().execute("PF('editDialog').hide();");
        } catch (RestrictionException e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), e.getMessage()));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void lockAction() {
        try {
            userService.lockUser(selectedUser);
            refreshList();
        } catch (RestrictionException e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), e.getMessage()));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void unlockAction() {
        try {
            userService.unlockUser(selectedUser);
            refreshList();
        } catch (RestrictionException e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), e.getMessage()));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void setPassDialog() {
        if (selectedUser == null) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), bundle.getString("error.user.NotSelected")));
            return;
        }

        if (selectedUser.getId() == 0) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), bundle.getString("error.operation.NotAllowed")));
            return;
        }

        RequestContext.getCurrentInstance().execute("PF('setPassDialog').show()");
    }

    public void setPassAction() {
        try {
            userService.setUserPass(selectedUser, newPass);
            newPass = "";

            refreshList();
            RequestContext.getCurrentInstance().execute("PF('setPassDialog').hide();");
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void addValue() {
        if (selectGroup.isEmpty()) {
            return;
        }

        User.LinkGroup linkGroup = new User.LinkGroup();
        linkGroup.setGroupId(selectGroupDefault.get(selectedGroup));
        linkGroup.setGroupName(selectedGroup);
        linkGroup.setParticipant(true);
        currUser.getLinkGroups().add(linkGroup);

        selectGroup.remove(selectedGroup);
    }

    public void removeValue(User.LinkGroup collectedList) {
        selectGroup.add(collectedList.getGroupName());
    }

    private List<String> getCurrSelectGroup(User currUser) {
        List<String> currSelectGroup = new ArrayList<>();
        currSelectGroup.addAll(selectGroupDefault.keySet());
        for (User.LinkGroup linkGroup: currUser.getLinkGroups()) {
            currSelectGroup.remove(linkGroup.getGroupName());
        }
        return currSelectGroup;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }

    public List<User> getUserList() {
        return userList;
    }

    public List<User> getFilteredUserList() {
        return filteredUserList;
    }

    public void setFilteredUserList(List<User> filteredUserList) {
        this.filteredUserList = filteredUserList;
    }

    public User getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(User selectedUser) {
        this.selectedUser = selectedUser;
    }

    public User getCurrUser() {
        return currUser;
    }

    public void setCurrUser(User currUser) {
        this.currUser = currUser;
    }

    public String getNewPass() {
        return newPass;
    }

    public void setNewPass(String newPass) {
        this.newPass = newPass;
    }

    public List<String> getSelectGroup() {
        return selectGroup;
    }

    public String getSelectedGroup() {
        return selectedGroup;
    }

    public void setSelectedGroup(String selectedGroup) {
        this.selectedGroup = selectedGroup;
    }
}