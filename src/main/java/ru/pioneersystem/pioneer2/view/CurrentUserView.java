package ru.pioneersystem.pioneer2.view;

import org.primefaces.context.RequestContext;
import ru.pioneersystem.pioneer2.service.CurrentUser;
import ru.pioneersystem.pioneer2.service.UserService;
import ru.pioneersystem.pioneer2.service.exception.PasswordException;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

@ManagedBean
@ViewScoped
public class CurrentUserView implements Serializable {
    private static final long serialVersionUID = 1L;

    private String login;
    private String pass;
    private String newPass;

    private int currDocId;
    private boolean showRoute;

    private ResourceBundle bundle;

    @ManagedProperty("#{currentUser}")
    private CurrentUser currentUser;

    @ManagedProperty("#{userService}")
    private UserService userService;

    @PostConstruct
    public void init() {
        bundle = ResourceBundle.getBundle("text", FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }

    public void signInAction() {
        try {
            currentUser.signIn(login, pass);
            pass = null;

            currentUser.selectMenu(currentUser.getCurrMenuId());

            RequestContext.getCurrentInstance().execute("PF('loginDialog').hide()");
            RequestContext.getCurrentInstance().update(
                    new ArrayList<>(Arrays.asList(new String[] {"northPanel", "leftPanel", "centerPanel", "dialogsPanel", "autoOpen"})));
        }
        catch (PasswordException e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), e.getMessage()));
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void changePassAction() {
        try {
            userService.changeUserPass(login, pass, newPass);

            pass = null;
            newPass = null;

            RequestContext.getCurrentInstance().execute("PF('changePassDialog').hide()");
        }
        catch (PasswordException e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), e.getMessage()));
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void setCurrentUser(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
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

    public int getCurrDocId() {
        return currDocId;
    }

    public void setCurrDocId(int currDocId) {
        this.currDocId = currDocId;
    }

    public boolean isShowRoute() {
        return showRoute;
    }

    public void setShowRoute(boolean showRoute) {
        this.showRoute = showRoute;
    }
}