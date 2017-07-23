package ru.pioneersystem.pioneer2.view;

import org.primefaces.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.model.Menu;
import ru.pioneersystem.pioneer2.model.Part;
import ru.pioneersystem.pioneer2.model.User;
import ru.pioneersystem.pioneer2.service.*;
import ru.pioneersystem.pioneer2.service.exception.PasswordException;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.*;

@Service("currentUser")
@Scope(value="session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CurrentUser implements Serializable {
    private static final long serialVersionUID = 1L;

    private int screenHeight;
    private int screenWidth;

    private String login;
    private String pass;
    private String newPass;
    private boolean logged;
    private User user;
    private List<Menu> userMenu;
    private Map<String, Integer> userCreateGroups;
    private Map<String, Integer> userRoutes;
    private Map<String, Integer> userPubParts;

    private String currPage = "welcome.xhtml";
    private int currMenuIndex = -1;
    private int currMenuId;
    private Menu currMenu;

    private UserService userService;
    private MenuService menuService;
    private GroupService groupService;
    private RouteService routeService;
    private PartService partService;
    private LocaleBean localeBean;
    private HttpServletRequest request;

    @Autowired
    public CurrentUser(UserService userService, MenuService menuService, GroupService groupService,
                       RouteService routeService, PartService partService, LocaleBean localeBean,
                       HttpServletRequest request) {
        this.userService = userService;
        this.menuService = menuService;
        this.groupService = groupService;
        this.routeService = routeService;
        this.partService = partService;
        this.localeBean = localeBean;
        this.request = request;
    }

    public void signIn() {
        try {
            int userId = userService.getUserId(login, pass);
            user = userService.getUserWithCompany(userId);

            if (user.getState() == 0) {
                showGrowl(FacesMessage.SEVERITY_WARN, "warn", "warn.user.locked");
                return;
            }

            if (user.getCompany().getState() == 0) {
                showGrowl(FacesMessage.SEVERITY_WARN, "warn", "warn.company.locked");
                return;
            }

            userMenu = menuService.getUserMenu(userId);
            userCreateGroups = groupService.getUserCreateGroupsMap();
            userRoutes = routeService.getUserRoutesMap();
            userPubParts = partService.getUserPartMap(Part.Type.FOR_DOCUMENTS);

            // TODO: 03.04.2017 Добавить начальных данных пользователя, которых не хватает

//            RequestContextHolder.currentRequestAttributes().setAttribute(SessionListener.USER_ID, user.getId(), RequestAttributes.SCOPE_SESSION);
//            RequestContextHolder.currentRequestAttributes().setAttribute(SessionListener.COMPANY_ID, user.getCompanyId(), RequestAttributes.SCOPE_SESSION);
            request.getSession().setAttribute(SessionListener.USER_ID, user.getId());
            request.getSession().setAttribute(SessionListener.COMPANY_ID, user.getCompanyId());

            login = null;
            pass = null;
            logged = true;

            RequestContext.getCurrentInstance().execute("PF('loginDialog').hide()");
            RequestContext.getCurrentInstance().update(
                    new ArrayList<>(Arrays.asList(new String[] {"northPanel", "leftPanel", "centerPanel", "dialogsPanel"})));
        } catch (PasswordException e) {
            showGrowl(FacesMessage.SEVERITY_WARN, "warn", "warn.login.or.pass.not.valid");
        } catch (ServiceException e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.pass.not.checked");
        }
    }

    public void changePass() {
        try {
            userService.changeUserPass(user.getLogin(), pass, newPass);
            pass = null;
            newPass = null;

            showGrowl(FacesMessage.SEVERITY_INFO, "info", "info.changePass.success");
            RequestContext.getCurrentInstance().execute("PF('changePassDialog').hide()");
        } catch (PasswordException e) {
            showGrowl(FacesMessage.SEVERITY_ERROR, "error", "error.oldPass.not.valid");
        } catch (ServiceException e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.pass.not.checked");
        }
    }

    public void setCurrMenuId(int currMenuId) {
        this.currMenuId = currMenuId;

        if (userMenu == null) {
            return;
        }

        int menuIndex = 0;
        for (Menu menu: userMenu) {
            if (menu.getId() == currMenuId) {
                currMenuIndex = menuIndex;
                currMenu = menu;
                setCurrPage(currMenu.getPage());
                break;
            }
            menuIndex = menuIndex + 1;
        }
    }

    public void setCurrPage(String currPage) {
        if (currPage == null || currPage.equals("")) {
            return;
        }
        this.currPage = currPage;
//        RequestContext.getCurrentInstance().update(
//                new ArrayList<>(Arrays.asList(new String[] {"leftPanel", "centerPanel", "dialogsPanel"})));
    }

    private void showGrowl(FacesMessage.Severity severity, String shortMessage, String longMessage) {
        ResourceBundle bundle = ResourceBundle.getBundle("text", localeBean.getLocale());
        FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(
                severity, bundle.getString(shortMessage), bundle.getString(longMessage)));
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getNewPass() {
        return newPass;
    }

    public void setNewPass(String newPass) {
        this.newPass = newPass;
    }

    public boolean isLogged() {
        return logged;
    }

    public User getUser() {
        return user;
    }

    public List<Menu> getUserMenu() {
        return userMenu;
    }

    public Map<String, Integer> getUserCreateGroups() {
        return userCreateGroups;
    }

    public Map<String, Integer> getUserRoutes() {
        return userRoutes;
    }

    public Map<String, Integer> getUserPubParts() {
        return userPubParts;
    }

    public String getCurrPage() {
        return currPage;
    }

    public int getCurrMenuIndex() {
        return currMenuIndex;
    }

    public void setCurrMenuIndex(int currMenuIndex) {
        this.currMenuIndex = currMenuIndex;
    }

    public int getCurrMenuId() {
        return currMenuId;
    }

    public Menu getCurrMenu() {
        return currMenu;
    }
}