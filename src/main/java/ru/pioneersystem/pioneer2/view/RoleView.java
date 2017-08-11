package ru.pioneersystem.pioneer2.view;

import org.primefaces.context.RequestContext;
import ru.pioneersystem.pioneer2.model.Role;
import ru.pioneersystem.pioneer2.service.RoleService;

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

    private Role currRole;

    private ResourceBundle bundle;

    @ManagedProperty("#{roleService}")
    private RoleService roleService;

    @PostConstruct
    public void init() {
        bundle = ResourceBundle.getBundle("text", FacesContext.getCurrentInstance().getViewRoot().getLocale());
        refreshList();
    }

    public void refreshList() {
        try {
            roleList = roleService.getRoleList();
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void newDialog() {
        currRole = roleService.getNewRole();

        RequestContext.getCurrentInstance().execute("PF('editDialog').show()");
    }

    public void editDialog() {
        if (selectedRole == null) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), bundle.getString("error.role.NotSelected")));
            return;
        }

        if (selectedRole.getState() == Role.State.SYSTEM) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), bundle.getString("warn.system.edit.restriction")));
            return;
        }

        try {
            currRole = roleService.getRole(selectedRole.getId());
            RequestContext.getCurrentInstance().execute("PF('editDialog').show()");
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void saveAction() {
        try {
            roleService.saveRole(currRole);

            refreshList();
            RequestContext.getCurrentInstance().execute("PF('editDialog').hide();");
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void deleteDialog() {
        if (selectedRole == null) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), bundle.getString("error.role.NotSelected")));
            return;
        }

        if (selectedRole.getState() == Role.State.SYSTEM) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), bundle.getString("warn.system.edit.restriction")));
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
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }

        RequestContext.getCurrentInstance().execute("PF('deleteDialog').hide();");
    }

    public void setRoleService(RoleService roleService) {
        this.roleService = roleService;
    }

    public List<Role> getRoleList() {
        return roleList;
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
}