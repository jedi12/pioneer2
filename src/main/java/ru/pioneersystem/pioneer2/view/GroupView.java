package ru.pioneersystem.pioneer2.view;

import org.primefaces.context.RequestContext;
import ru.pioneersystem.pioneer2.model.Group;
import ru.pioneersystem.pioneer2.service.*;
import ru.pioneersystem.pioneer2.service.exception.RestrictionException;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.*;

@ManagedBean
@ViewScoped
public class GroupView implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Group> groupList;
    private List<Group> filteredGroup;
    private Group selectedGroup;

    private Group currGroup;

    private Map<String, Integer> selectRole;
    private List<String> selectUser;
    private Map<String, Integer> selectUserDefault;
    private String selectedUser;

    private List<String> routesWithGroup;
    private List<String> docToCancel;
    private int countPartsWithRestriction;
    private int countRoutesWithRestriction;

    private ResourceBundle bundle;

    @ManagedProperty("#{groupService}")
    private GroupService groupService;

    @ManagedProperty("#{partService}")
    private PartService partService;

    @ManagedProperty("#{roleService}")
    private RoleService roleService;

    @ManagedProperty("#{routeService}")
    private RouteService routeService;

    @ManagedProperty("#{documentService}")
    private DocumentService documentService;

    @ManagedProperty("#{userService}")
    private UserService userService;

    @PostConstruct
    public void init() {
        bundle = ResourceBundle.getBundle("text", FacesContext.getCurrentInstance().getViewRoot().getLocale());
        currGroup = groupService.getNewGroup();
    }

    public void refreshList() {
        try {
            groupList = groupService.getGroupList();
            selectRole = roleService.getRoleMap();
            selectUserDefault = userService.getUserMap();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void newDialog() {
        currGroup = groupService.getNewGroup();
        selectUser = getCurrSelectUser(currGroup);

        RequestContext.getCurrentInstance().execute("PF('editDialog').show()");
    }

    public void editDialog() {
        try {
            currGroup = groupService.getGroup(selectedGroup);
            selectUser = getCurrSelectUser(currGroup);

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
            groupService.saveGroup(currGroup);

            refreshList();
            RequestContext.getCurrentInstance().execute("PF('editDialog').hide();");
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void deleteDialog() {
        if (selectedGroup == null) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), bundle.getString("error.group.NotSelected")));
            return;
        }

        try {
            routesWithGroup = routeService.getRoutesWithGroup(selectedGroup.getId());
            docToCancel = documentService.getDocToCancelByGroup(selectedGroup.getId());
            if (routesWithGroup.isEmpty() && docToCancel.isEmpty()) {
                countPartsWithRestriction = partService.getCountPartsWithRestriction(selectedGroup.getId());
                countRoutesWithRestriction = routeService.getCountRoutesWithRestriction(selectedGroup.getId());
            }

            RequestContext.getCurrentInstance().execute("PF('deleteDialog').show()");
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void deleteAction() {
        try {
            groupService.deleteGroup(selectedGroup);
            refreshList();
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }

        RequestContext.getCurrentInstance().execute("PF('deleteDialog').hide();");
    }

    public void addValue() {
        if (selectUser.isEmpty()) {
            return;
        }

        Group.LinkUser linkUser = new Group.LinkUser();
        linkUser.setUserId(selectUserDefault.get(selectedUser));
        linkUser.setUserName(selectedUser);
        linkUser.setParticipant(true);
        currGroup.getLinkUsers().add(linkUser);

        selectUser.remove(selectedUser);
    }

    public void removeValue(Group.LinkUser collectedList) {
        selectUser.add(collectedList.getUserName());
    }

    private List<String> getCurrSelectUser(Group currGroup) {
        List<String> currSelectUser = new ArrayList<>();
        currSelectUser.addAll(selectUserDefault.keySet());
        for (Group.LinkUser linkUser: currGroup.getLinkUsers()) {
            currSelectUser.remove(linkUser.getUserName());
        }
        return currSelectUser;
    }

    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }

    public void setPartService(PartService partService) {
        this.partService = partService;
    }

    public void setRoleService(RoleService roleService) {
        this.roleService = roleService;
    }

    public void setRouteService(RouteService routeService) {
        this.routeService = routeService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public List<Group> getGroupList() {
        return groupList;
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

    public List<String> getRoutesWithGroup() {
        return routesWithGroup;
    }

    public List<String> getDocToCancel() {
        return docToCancel;
    }

    public int getCountPartsWithRestriction() {
        return countPartsWithRestriction;
    }

    public int getCountRoutesWithRestriction() {
        return countRoutesWithRestriction;
    }
}