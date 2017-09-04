package ru.pioneersystem.pioneer2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.model.*;
import ru.pioneersystem.pioneer2.service.exception.PasswordException;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Service("currentUser")
@Scope(value="session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CurrentUser implements Serializable {
    private static final long serialVersionUID = 1L;

    private int screenHeight;
    private int screenWidth;

    private boolean logged;
    private User user;
    private List<Menu> userMenu;
    private Map<String, Integer> userCreateGroups;
    private Map<String, Integer> userRoutes;
    private Map<String, Integer> userPubParts;
    private Map<Integer, Role> userRoles;
    private Map<Integer, Map<Integer, Integer>> userRolesGroupActivity;

    private String currPage = "welcome.xhtml";
    private int currMenuIndex = -1;
    private int currMenuId;
    private Menu currMenu;
    private Role currRole;

    private boolean superRole;
    private boolean adminRole;
    private boolean userRole;
    private boolean publicRole;

    private EventService eventService;
    private UserService userService;
    private MenuService menuService;
    private GroupService groupService;
    private RouteService routeService;
    private PartService partService;
    private RoleService roleService;
    private LocaleBean localeBean;
    private MessageSource messageSource;

    @Autowired
    public CurrentUser(EventService eventService, UserService userService, MenuService menuService,
                       GroupService groupService, RouteService routeService, PartService partService,
                       RoleService roleService, LocaleBean localeBean, MessageSource messageSource) {
        this.eventService = eventService;
        this.userService = userService;
        this.menuService = menuService;
        this.groupService = groupService;
        this.routeService = routeService;
        this.partService = partService;
        this.roleService = roleService;
        this.localeBean = localeBean;
        this.messageSource = messageSource;
    }

    public void signIn(String login, String pass) throws ServiceException {
        try {
            int userId = userService.checkLoginAndPass(login, pass);
            user = userService.getUserWithCompany(userId);

            if (user.getState() == 0) {
                String mess = messageSource.getMessage("warn.user.locked", null, localeBean.getLocale());
                eventService.logEvent(Event.Type.USER_TRY_SIGN_IN, 0, mess);
                throw new PasswordException(mess);
            }

            if (user.getCompany().getState() == 0) {
                String mess = messageSource.getMessage("warn.company.locked", null, localeBean.getLocale());
                eventService.logEvent(Event.Type.USER_TRY_SIGN_IN, 0, mess);
                throw new PasswordException(mess);
            }

            userMenu = menuService.getUserMenu();
            userCreateGroups = groupService.getUserCreateGroupsMap();
            userRoutes = routeService.getUserRoutesMap();
            userPubParts = partService.getUserPartMap(Part.Type.FOR_DOCUMENTS);
            userRolesGroupActivity = groupService.getUserRolesGroupActivityMap();

            userRoles = roleService.getUserRoleMap();
            for (Role role: userRoles.values()) {
                switch (role.getType()) {
                    case Role.Type.SUPER:
                        superRole = true;
                        break;
                    case Role.Type.ADMIN:
                        adminRole = true;
                        break;
                    case Role.Type.USER:
                        userRole = true;
                        break;
                    case Role.Type.PUBLIC:
                        publicRole = true;
                        break;
                }
            }

            // TODO: 03.04.2017 Добавить начальных данных пользователя, которых не хватает

//            RequestContextHolder.currentRequestAttributes().setAttribute(SessionListener.USER_ID, user.getId(), RequestAttributes.SCOPE_SESSION);
//            RequestContextHolder.currentRequestAttributes().setAttribute(SessionListener.COMPANY_ID, user.getCompanyId(), RequestAttributes.SCOPE_SESSION);

//            request.getSession().setAttribute(SessionListener.USER_ID, user.getId());
//            request.getSession().setAttribute(SessionListener.COMPANY_ID, user.getCompanyId());

            String ipAddress = getIpAddress();
            // TODO: 02.09.2017 Переделать через Spring?
            HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
            session.setAttribute(SessionListener.USER_ID, user.getId());
            session.setAttribute(SessionListener.COMPANY_ID, user.getCompanyId());
            session.setAttribute(SessionListener.IP_ADDRESS, ipAddress);

            logged = true;

            eventService.logEvent(Event.Type.USER_SIGNED_IN, 0, "IP: " + ipAddress);

        } catch (PasswordException e) {
            String mess = messageSource.getMessage("login.login.label", null, localeBean.getLocale()) +
                    ": " + login + ", IP: " + getIpAddress() + " - " + e.getMessage();
            eventService.logEvent(Event.Type.USER_TRY_SIGN_IN, 0, mess);
            throw new PasswordException(e.getMessage());
        } catch (ServiceException e) {
            String mess = messageSource.getMessage("login.login.label", null, localeBean.getLocale()) +
                    ": " + login + ", IP: " + getIpAddress() + " - " + e.getMessage();
            eventService.logEvent(Event.Type.USER_TRY_SIGN_IN, 0, mess);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    public void signOut() {
        try {
            // TODO: 02.09.2017 Переделать через Spring?
            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            externalContext.redirect("signedOut.xhtml");
            externalContext.setSessionMaxInactiveInterval(1);
        } catch (IOException e) {
            String mess = messageSource.getMessage("login.login.label", null, localeBean.getLocale()) +
                    ": " + user.getLogin() + ", IP: " + getIpAddress();
            eventService.logEvent(Event.Type.ERROR, 0, mess);
        }
    }

    private String getIpAddress() {
        // TODO: 02.09.2017 Переделать через Spring?
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    public void setCurrMenuId(int currMenuId) {
//        this.currMenuId = currMenuId;

        if (userMenu == null) {
            return;
        }

        int menuIndex = 0;
        for (Menu menu: userMenu) {
            if (menu.getId() == currMenuId) {
                currMenuIndex = menuIndex;
                currMenu = menu;
                setCurrPage(currMenu);
                break;
            }
            menuIndex = menuIndex + 1;
        }
    }

    public void setCurrPage(Menu menu) {
        if (menu.getPage() == null || menu.getPage().equals("")) {
            return;
        }
        this.currMenu = menu;
        this.currMenuId = menu.getId();
        this.currPage = menu.getPage();
        this.currRole = userRoles.get(menu.getRoleId());
//        RequestContext.getCurrentInstance().update(
//                new ArrayList<>(Arrays.asList(new String[] {"leftPanel", "centerPanel", "dialogsPanel"})));
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

    public Map<Integer, Role> getUserRoles() {
        return userRoles;
    }

    public Map<Integer, Map<Integer, Integer>> getUserRolesGroupActivity() {
        return userRolesGroupActivity;
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

    public Role getCurrRole() {
        return currRole;
    }

    public boolean isSuperRole() {
        return superRole;
    }

    public boolean isAdminRole() {
        return adminRole;
    }

    public boolean isUserRole() {
        return userRole;
    }

    public boolean isPublicRole() {
        return publicRole;
    }
}