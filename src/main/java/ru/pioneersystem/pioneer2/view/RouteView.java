package ru.pioneersystem.pioneer2.view;

import org.primefaces.context.RequestContext;
import ru.pioneersystem.pioneer2.model.Group;
import ru.pioneersystem.pioneer2.model.Route;
import ru.pioneersystem.pioneer2.service.GroupService;
import ru.pioneersystem.pioneer2.service.RouteService;

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
public class RouteView implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Route> routeList;
    private List<Route> filteredRoute;
    private Route selectedRoute;

    private boolean createFlag;
    private Route currRoute;

    private List<String> selectPoint;
    private Map<String, Group> selectPointDefault;
    private String selectedPoint;
    private int stage;

    private List<String> selectGroup;
    private Map<String, Integer> selectGroupDefault;
    private String selectedGroup;

    private ResourceBundle bundle;

    @ManagedProperty("#{routeService}")
    private RouteService routeService;

    @ManagedProperty("#{groupService}")
    private GroupService groupService;

    @PostConstruct
    public void init() {
        bundle = ResourceBundle.getBundle("text", FacesContext.getCurrentInstance().getViewRoot().getLocale());
        refreshList();
    }

    private void refreshList() {
        try {
            routeList = routeService.getRouteList();
            selectPointDefault = groupService.getPointMap();
            selectGroupDefault = groupService.getGroupMap();
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.list.refresh");
        }
    }

    public void newDialog() {
        createFlag = true;
        currRoute = new Route();
        currRoute.setPoints(new LinkedList<>());
        currRoute.setGroups(new LinkedList<>());
        selectPoint = getCurrSelectPoint(currRoute);
        selectGroup = getCurrSelectGroup(currRoute);

        RequestContext.getCurrentInstance().execute("PF('editDialog').show()");
    }

    public void editDialog() {
        createFlag = false;

        if (selectedRoute == null) {
            showGrowl(FacesMessage.SEVERITY_WARN, "warn", "error.list.element.not.selected");
            return;
        }

        try {
            currRoute = routeService.getRoute(selectedRoute.getId());
            selectPoint = getCurrSelectPoint(currRoute);
            selectGroup = getCurrSelectGroup(currRoute);

            RequestContext.getCurrentInstance().execute("PF('editDialog').show()");
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.element.not.loaded");
        }
    }

    public void saveAction() {
        try {
            if (createFlag) {
                routeService.createRoute(currRoute);
            } else {
                routeService.updateRoute(currRoute);
            }

            refreshList();
            RequestContext.getCurrentInstance().execute("PF('editDialog').hide();");
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.not.saved");
        }
    }

    public void deleteDialog() {
        if (selectedRoute == null) {
            showGrowl(FacesMessage.SEVERITY_WARN, "warn", "error.list.element.not.selected");
            return;
        }

        RequestContext.getCurrentInstance().execute("PF('deleteDialog').show()");
    }

    public void deleteAction() {
        try {
            routeService.deleteRoute(selectedRoute.getId());
            refreshList();
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.not.deleted");
        }

        RequestContext.getCurrentInstance().execute("PF('deleteDialog').hide();");
    }

    public void addValue() {
        if (selectPoint.isEmpty()) {
            return;
        }

        Route.Point point = new Route.Point();
        point.setStage(stage);
        point.setGroupId(selectPointDefault.get(selectedPoint).getId());
        point.setGroupName(selectedPoint);
        point.setRoleId(selectPointDefault.get(selectedPoint).getRoleId());
        point.setRoleName(selectPointDefault.get(selectedPoint).getRoleName());
        currRoute.getPoints().add(point);

        selectPoint.remove(selectedPoint);
    }

    public void removeValue(Route.Point collectedList) {
        selectPoint.add(collectedList.getGroupName());
    }

    public void addGroup() {
        if (selectGroup.isEmpty()) {
            return;
        }

        Route.LinkGroup linkGroup = new Route.LinkGroup();
        linkGroup.setGroupId(selectGroupDefault.get(selectedGroup));
        linkGroup.setGroupName(selectedGroup);
        currRoute.getGroups().add(linkGroup);

        selectGroup.remove(selectedGroup);
    }

    public void removeGroup(Route.LinkGroup collectedGroup) {
        selectGroup.add(collectedGroup.getGroupName());
    }

    private List<String> getCurrSelectPoint(Route currRoute) {
        List<String> currSelectRoute = new LinkedList<>();
        currSelectRoute.addAll(selectPointDefault.keySet());
        for (Route.Point point: currRoute.getPoints()) {
            currSelectRoute.remove(point.getGroupName());
        }
        return currSelectRoute;
    }

    private List<String> getCurrSelectGroup(Route currRoute) {
        List<String> currSelectGroup = new LinkedList<>();
        currSelectGroup.addAll(selectGroupDefault.keySet());
        for (Route.LinkGroup linkGroup : currRoute.getGroups()) {
            currSelectGroup.remove(linkGroup.getGroupName());
        }
        return currSelectGroup;
    }

    private void showGrowl(FacesMessage.Severity severity, String shortMessage, String longMessage) {
        FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(
                severity, bundle.getString(shortMessage), bundle.getString(longMessage)));
    }

    public void setRouteService(RouteService routeService) {
        this.routeService = routeService;
    }

    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }

    public List<Route> getRouteList() {
        return routeList;
    }

    public List<Route> getFilteredRoute() {
        return filteredRoute;
    }

    public void setFilteredRoute(List<Route> filteredRoute) {
        this.filteredRoute = filteredRoute;
    }

    public Route getSelectedRoute() {
        return selectedRoute;
    }

    public void setSelectedRoute(Route selectedRoute) {
        this.selectedRoute = selectedRoute;
    }

    public boolean isCreateFlag() {
        return createFlag;
    }

    public Route getCurrRoute() {
        return currRoute;
    }

    public void setCurrRoute(Route currRoute) {
        this.currRoute = currRoute;
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public String getSelectedPoint() {
        return selectedPoint;
    }

    public void setSelectedPoint(String selectedPoint) {
        this.selectedPoint = selectedPoint;
    }

    public List<String> getSelectPoint() {
        return selectPoint;
    }

    public void setSelectPoint(List<String> selectPoint) {
        this.selectPoint = selectPoint;
    }

    public List<String> getSelectGroup() {
        return selectGroup;
    }

    public void setSelectGroup(List<String> selectGroup) {
        this.selectGroup = selectGroup;
    }

    public String getSelectedGroup() {
        return selectedGroup;
    }

    public void setSelectedGroup(String selectedGroup) {
        this.selectedGroup = selectedGroup;
    }
}