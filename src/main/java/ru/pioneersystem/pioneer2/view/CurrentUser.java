package ru.pioneersystem.pioneer2.view;

import org.primefaces.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.model.Menu;
import ru.pioneersystem.pioneer2.model.User;
import ru.pioneersystem.pioneer2.service.MenuService;
import ru.pioneersystem.pioneer2.service.SessionListener;
import ru.pioneersystem.pioneer2.service.UserService;
import ru.pioneersystem.pioneer2.service.exception.PasswordException;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

@Service("currentUser")
@Scope(value="session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CurrentUser implements Serializable {
    private static final long serialVersionUID = 1L;

    private String login;
    private String pass;
    private String newPass;
    private boolean logged;
    private User user;
    private List<Menu> userMenu;
    private String currPage = "welcome.xhtml";

    private UserService userService;
    private MenuService menuService;
    private LocaleBean localeBean;
    private HttpServletRequest request;

    @Autowired
    public CurrentUser(UserService userService, MenuService menuService, LocaleBean localeBean, HttpServletRequest request) {
        this.userService = userService;
        this.menuService = menuService;
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


    private void showGrowl(FacesMessage.Severity severity, String shortMessage, String longMessage) {
        ResourceBundle bundle = ResourceBundle.getBundle("text", localeBean.getLocale());
        FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(
                severity, bundle.getString(shortMessage), bundle.getString(longMessage)));
    }

    public void setCurrPage(String currPage) {
        if (currPage == null || currPage.equals("")) {
            return;
        }
        this.currPage = currPage;
        RequestContext.getCurrentInstance().update("centerPanel");
        RequestContext.getCurrentInstance().update("dialogsPanel");
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

    public String getCurrPage() {
        return currPage;
    }
}
