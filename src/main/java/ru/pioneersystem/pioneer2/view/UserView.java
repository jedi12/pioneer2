package ru.pioneersystem.pioneer2.view;

import org.primefaces.context.RequestContext;
import ru.pioneersystem.pioneer2.model.User;
import ru.pioneersystem.pioneer2.service.UserService;
import ru.pioneersystem.pioneer2.service.exception.RestrictionException;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.List;
import java.util.ResourceBundle;

@ManagedBean
@ViewScoped
public class UserView implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<User> userList;
    private List<User> filteredUserList;
    private User selectedUser;

    private boolean createFlag;
    private User currUser;

    private String newPass;

    @ManagedProperty("#{userService}")
    private UserService userService;

    @ManagedProperty("#{localeBean}")
    private LocaleBean localeBean;

    @ManagedProperty("#{currentUser}")
    private CurrentUser currentUser;

    @PostConstruct
    public void init()  {
        refreshList();
    }

    private void refreshList() {
        try {
            userList = userService.getUserList(currentUser.getUser().getCompanyId(), localeBean.getLocale());
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.list.refresh");
        }
    }

    public void newDialog() {
        createFlag = true;
        currUser = new User();

        RequestContext.getCurrentInstance().execute("PF('editDialog').show()");
    }

    public void editDialog() {
        createFlag = false;

        if (selectedUser == null) {
            showGrowl(FacesMessage.SEVERITY_WARN, "warn", "error.list.element.not.selected");
            return;
        }

        try {
            currUser = userService.getUserWithCompany(selectedUser.getId(), localeBean.getLocale());
            RequestContext.getCurrentInstance().execute("PF('editDialog').show()");
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.element.not.loaded");
        }
    }

    public void saveAction() {
        try {
            if (createFlag) {
                userService.createUser(currUser, currentUser.getUser().getCompanyId());
            } else {
                userService.updateUser(currUser);
            }

            refreshList();
            RequestContext.getCurrentInstance().execute("PF('editDialog').hide();");
        }
        catch (RestrictionException e) {
            showGrowl(FacesMessage.SEVERITY_WARN, "warn", "warn.user.max.restriction");
        }
        catch (ServiceException e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.not.saved");
        }
    }

    public void lockAction() {
        if (selectedUser == null) {
            showGrowl(FacesMessage.SEVERITY_WARN, "warn", "error.list.element.not.selected");
            return;
        }

        try {
            userService.lockUser(selectedUser.getId());
            refreshList();
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.not.locked");
        }
    }

    public void unlockAction() {
        if (selectedUser == null) {
            showGrowl(FacesMessage.SEVERITY_WARN, "warn", "error.list.element.not.selected");
            return;
        }

        try {
            userService.unlockUser(selectedUser.getId(), currentUser.getUser().getCompanyId());
            refreshList();
        }
        catch (RestrictionException e) {
                showGrowl(FacesMessage.SEVERITY_WARN, "warn", "warn.user.max.restriction");
            }
        catch (ServiceException e) {
                showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.not.unlocked");
            }
    }

    public void setPassDialog() {
        if (selectedUser == null) {
            showGrowl(FacesMessage.SEVERITY_WARN, "warn", "error.list.element.not.selected");
            return;
        }

        RequestContext.getCurrentInstance().execute("PF('setPassDialog').show()");
    }

    public void setPassAction() {
        try {
            userService.setUserPass(selectedUser.getId(), newPass);
            newPass = "";

            refreshList();
            RequestContext.getCurrentInstance().execute("PF('setPassDialog').hide();");
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.not.pass.changed");
        }
    }

    private void showGrowl(FacesMessage.Severity severity, String shortMessage, String longMessage) {
        ResourceBundle bundle = ResourceBundle.getBundle("text", localeBean.getLocale());
        FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(
                severity, bundle.getString(shortMessage), bundle.getString(longMessage)));
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setLocaleBean(LocaleBean localeBean) {
        this.localeBean = localeBean;
    }

    public void setCurrentUser(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    public List<User> getUserList() {
        return userList;
    }

    public boolean isCreateFlag() {
        return createFlag;
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
}