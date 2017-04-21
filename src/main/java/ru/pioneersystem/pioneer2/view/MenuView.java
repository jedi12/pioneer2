package ru.pioneersystem.pioneer2.view;

import org.primefaces.context.RequestContext;
import ru.pioneersystem.pioneer2.model.Menu;
import ru.pioneersystem.pioneer2.service.MenuService;
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
public class MenuView implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Menu> menuList;
    private List<Menu> filteredMenuList;
    private Menu selectedMenu;

    private boolean createFlag;
    private Menu currMenu;

    @ManagedProperty("#{menuService}")
    private MenuService menuService;

    @ManagedProperty("#{localeBean}")
    private LocaleBean localeBean;

    @PostConstruct
    public void init()  {
        refreshList();
    }

    private void refreshList() {
        try {
            menuList = menuService.getMenuList();
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.list.refresh");
        }
    }

    public void newDialog() {
        createFlag = true;
        currMenu = new Menu();

        RequestContext.getCurrentInstance().execute("PF('editDialog').show()");
    }

    public void editDialog() {
        createFlag = false;

        if (selectedMenu == null) {
            showGrowl(FacesMessage.SEVERITY_WARN, "warn", "error.list.element.not.selected");
            return;
        }

        if (selectedMenu.getState() >= Menu.State.SYSTEM) {
            showGrowl(FacesMessage.SEVERITY_WARN, "warn", "warn.system.edit.restriction");
            return;
        }

        try {
            currMenu = menuService.getMenu(selectedMenu.getId());
            RequestContext.getCurrentInstance().execute("PF('editDialog').show()");
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.element.not.loaded");
        }
    }

    public void saveAction() {
        try {
            if (createFlag) {
                menuService.createMenu(currMenu);
            } else {
                menuService.updateMenu(currMenu);
            }

            refreshList();
            RequestContext.getCurrentInstance().execute("PF('editDialog').hide();");
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.not.saved");
        }
    }

    public void deleteDialog() {
        if (selectedMenu == null) {
            showGrowl(FacesMessage.SEVERITY_WARN, "warn", "error.list.element.not.selected");
            return;
        }

        if (selectedMenu.getState() >= Menu.State.SYSTEM) {
            showGrowl(FacesMessage.SEVERITY_WARN, "warn", "warn.system.edit.restriction");
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
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.not.deleted");
        }

        RequestContext.getCurrentInstance().execute("PF('deleteDialog').hide();");
    }

    private void showGrowl(FacesMessage.Severity severity, String shortMessage, String longMessage) {
        ResourceBundle bundle = ResourceBundle.getBundle("text", FacesContext.getCurrentInstance().getViewRoot().getLocale());
        FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(
                severity, bundle.getString(shortMessage), bundle.getString(longMessage)));
    }

    public void setMenuService(MenuService menuService) {
        this.menuService = menuService;
    }

    public void setLocaleBean(LocaleBean localeBean) {
        this.localeBean = localeBean;
    }

    public List<Menu> getMenuList() {
        return menuList;
    }

    public boolean isCreateFlag() {
        return createFlag;
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