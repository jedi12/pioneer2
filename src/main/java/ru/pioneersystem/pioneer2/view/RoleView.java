package ru.pioneersystem.pioneer2.view;

import org.primefaces.context.RequestContext;
import ru.pioneersystem.pioneer2.model.Role;
import ru.pioneersystem.pioneer2.model.Status;
import ru.pioneersystem.pioneer2.service.RoleService;
import ru.pioneersystem.pioneer2.service.StatusService;
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
public class RoleView implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Role> roleList;
    private List<Role> filteredRoleList;
    private Role selectedRole;

    private boolean createFlag;
    private Role currRole;
    private Status currStatus;

    @ManagedProperty("#{roleService}")
    private RoleService roleService;

    @ManagedProperty("#{statusService}")
    private StatusService statusService;

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
            roleList = roleService.getRoleList(currentUser.getUser().getCompanyId(), localeBean.getLocale());
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.list.refresh");
        }
    }

    public void newDialog() {
        createFlag = true;
        currRole = new Role();
        currStatus = new Status();

        RequestContext.getCurrentInstance().execute("PF('editDialog').show()");
    }

    public void editDialog() {
        createFlag = false;

        if (selectedRole == null) {
            showGrowl(FacesMessage.SEVERITY_WARN, "warn", "error.list.element.not.selected");
            return;
        }

//        if (selectedRole.getState() == 2) {
//            showGrowl(FacesMessage.SEVERITY_WARN, "warn", "error.list.element.not.selected");
//            return;
//        }

        try {
            currRole = roleService.getRole(selectedRole.getId(), localeBean.getLocale());
            currStatus = statusService.getStatus(selectedRole.getId(), localeBean.getLocale());
            RequestContext.getCurrentInstance().execute("PF('editDialog').show()");
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.element.not.loaded");
        }
    }

    public void saveAction() {
        try {
            if (createFlag) {
                roleService.createRole(currRole, currStatus, currentUser.getUser().getCompanyId());
            } else {
                roleService.updateRole(currRole, currStatus);
            }

            refreshList();
            RequestContext.getCurrentInstance().execute("PF('editDialog').hide();");
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.not.saved");
        }
    }

    public void deleteDialog() {
        if (selectedRole == null) {
            showGrowl(FacesMessage.SEVERITY_WARN, "warn", "error.list.element.not.selected");
            return;
        }

        RequestContext.getCurrentInstance().execute("PF('deleteDialog').show()");
    }

    public void deleteAction() {
        try {
            roleService.deleteRole(selectedRole.getId());
            refreshList();
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.not.deleted");
        }

        RequestContext.getCurrentInstance().execute("PF('deleteDialog').hide();");
    }

    private void showGrowl(FacesMessage.Severity severity, String shortMessage, String longMessage) {
        ResourceBundle bundle = ResourceBundle.getBundle("text", FacesContext.getCurrentInstance().getViewRoot().getLocale());
        FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(
                severity, bundle.getString(shortMessage), bundle.getString(longMessage)));
    }

    public void setRoleService(RoleService roleService) {
        this.roleService = roleService;
    }

    public void setStatusService(StatusService statusService) {
        this.statusService = statusService;
    }

    public void setLocaleBean(LocaleBean localeBean) {
        this.localeBean = localeBean;
    }

    public void setCurrentUser(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    public List<Role> getRoleList() {
        return roleList;
    }

    public boolean isCreateFlag() {
        return createFlag;
    }

    public List<Role> getFilteredRoleList() {
        return filteredRoleList;
    }

    public void setFilteredRoleList(List<Role> filteredRoleList) {
        this.filteredRoleList = filteredRoleList;
    }

    public Role getSelectedRole() {
        return selectedRole;
    }

    public void setSelectedRole(Role selectedRole) {
        this.selectedRole = selectedRole;
    }

    public Role getCurrRole() {
        return currRole;
    }

    public void setCurrRole(Role currRole) {
        this.currRole = currRole;
    }

    public Status getCurrStatus() {
        return currStatus;
    }

    public void setCurrStatus(Status currStatus) {
        this.currStatus = currStatus;
    }
}