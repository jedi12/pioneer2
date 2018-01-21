package ru.pioneersystem.pioneer2.view;

import org.primefaces.context.RequestContext;
import ru.pioneersystem.pioneer2.model.Group;
import ru.pioneersystem.pioneer2.model.Route;
import ru.pioneersystem.pioneer2.service.GroupService;
import ru.pioneersystem.pioneer2.service.RouteService;
import ru.pioneersystem.pioneer2.service.TemplateService;
import ru.pioneersystem.pioneer2.service.exception.RestrictionException;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.util.*;

@ManagedBean
@ViewScoped
public class RouteView {
    private List<Route> routeList;
    private List<Route> filteredRoute;
    private Route selectedRoute;

    private Route currRoute;

    private List<String> selectPoint;
    private Map<String, Group> selectPointDefault;
    private String selectedPoint;
    private int stage;

    private List<String> selectGroup;
    private Map<String, Integer> selectGroupDefault;
    private String selectedGroup;

    private List<String> templatesWithRoute;

    private ResourceBundle bundle;

    @ManagedProperty("#{routeService}")
    private RouteService routeService;

    @ManagedProperty("#{groupService}")
    private GroupService groupService;

    @ManagedProperty("#{templateService}")
    private TemplateService templateService;

    @PostConstruct
    public void init() {
        bundle = ResourceBundle.getBundle("text", FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }

    public void refreshList() {
        try {
            routeList = routeService.getRouteList();
            selectPointDefault = groupService.getPointMap();
            selectGroupDefault = groupService.getGroupMap();

            RequestContext.getCurrentInstance().execute("PF('routeTable').clearFilters()");
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void newDialog() {
        currRoute = routeService.getNewRoute();
        selectPoint = getCurrSelectPoint(currRoute);
        selectGroup = getCurrSelectGroup(currRoute);

        RequestContext.getCurrentInstance().execute("PF('editDialog').show()");
    }

    public void editDialog() {
        try {
            currRoute = routeService.getRoute(selectedRoute);
            selectPoint = getCurrSelectPoint(currRoute);
            selectGroup = getCurrSelectGroup(currRoute);

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
            routeService.saveRoute(currRoute);

            refreshList();
            RequestContext.getCurrentInstance().execute("PF('editDialog').hide();");
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void deleteDialog() {
        if (selectedRoute == null) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), bundle.getString("error.route.NotSelected")));
            return;
        }

        try {
            templatesWithRoute = templateService.getListContainingRoute(selectedRoute.getId());
            RequestContext.getCurrentInstance().execute("PF('deleteDialog').show()");
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void deleteAction() {
        try {
            routeService.deleteRoute(selectedRoute);
            refreshList();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
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

        for (Route.Point currPoint: currRoute.getPoints()) {
            if (currPoint.getStage() == point.getStage() && currPoint.getRoleId() != point.getRoleId()) {
                FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                        bundle.getString("warn"), bundle.getString("warn.route.badRoute")));
                return;
            }
        }

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
        List<String> currSelectRoute = new ArrayList<>();
        currSelectRoute.addAll(selectPointDefault.keySet());
        for (Route.Point point: currRoute.getPoints()) {
            currSelectRoute.remove(point.getGroupName());
        }
        return currSelectRoute;
    }

    private List<String> getCurrSelectGroup(Route currRoute) {
        List<String> currSelectGroup = new ArrayList<>();
        currSelectGroup.addAll(selectGroupDefault.keySet());
        for (Route.LinkGroup linkGroup : currRoute.getGroups()) {
            currSelectGroup.remove(linkGroup.getGroupName());
        }
        return currSelectGroup;
    }

    public void setRouteService(RouteService routeService) {
        this.routeService = routeService;
    }

    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }

    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
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

    public List<String> getTemplatesWithRoute() {
        return templatesWithRoute;
    }
}