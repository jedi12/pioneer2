package ru.pioneersystem.pioneer2.view;

import org.primefaces.context.RequestContext;
import ru.pioneersystem.pioneer2.model.Menu;
import ru.pioneersystem.pioneer2.service.MenuService;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.util.List;
import java.util.ResourceBundle;

@ManagedBean
@ViewScoped
public class MenuView {
    private List<Menu> menuList;
    private List<Menu> filteredMenuList;
    private Menu selectedMenu;

    private Menu currMenu;

    private ResourceBundle bundle;

    @ManagedProperty("#{menuService}")
    private MenuService menuService;

    @PostConstruct
    public void init() {
        bundle = ResourceBundle.getBundle("text", FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }

    public void refreshList() {
        try {
            menuList = menuService.getMenuList();
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void newDialog() {
        currMenu = menuService.getNewMenu();

        RequestContext.getCurrentInstance().execute("PF('editDialog').show()");
    }

    public void editDialog() {
        if (selectedMenu == null) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), bundle.getString("error.menu.NotSelected")));
            return;
        }

        if (selectedMenu.getState() >= Menu.State.SYSTEM) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), bundle.getString("warn.system.edit.restriction")));
            return;
        }

        try {
            currMenu = menuService.getMenu(selectedMenu.getId());
            RequestContext.getCurrentInstance().execute("PF('editDialog').show()");
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void saveAction() {
        try {
            menuService.saveMenu(currMenu);

            refreshList();
            RequestContext.getCurrentInstance().execute("PF('editDialog').hide();");
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void deleteDialog() {
        if (selectedMenu == null) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), bundle.getString("error.menu.NotSelected")));
            return;
        }

        if (selectedMenu.getState() >= Menu.State.SYSTEM) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), bundle.getString("warn.system.edit.restriction")));
            return;
        }

        RequestContext.getCurrentInstance().execute("PF('deleteDialog').show()");
    }

    public void deleteAction() {
        try {
            menuService.deleteMenu(selectedMenu.getId());
            refreshList();
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }

        RequestContext.getCurrentInstance().execute("PF('deleteDialog').hide();");
    }

    public void setMenuService(MenuService menuService) {
        this.menuService = menuService;
    }

    public List<Menu> getMenuList() {
        return menuList;
    }

    public List<Menu> getFilteredMenuList() {
        return filteredMenuList;
    }

    public void setFilteredMenuList(List<Menu> filteredMenuList) {
        this.filteredMenuList = filteredMenuList;
    }

    public Menu getSelectedMenu() {
        return selectedMenu;
    }

    public void setSelectedMenu(Menu selectedMenu) {
        this.selectedMenu = selectedMenu;
    }

    public Menu getCurrMenu() {
        return currMenu;
    }

    public void setCurrMenu(Menu currMenu) {
        this.currMenu = currMenu;
    }
}