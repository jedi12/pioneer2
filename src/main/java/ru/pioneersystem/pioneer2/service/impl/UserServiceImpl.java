package ru.pioneersystem.pioneer2.service.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.dao.CompanyDao;
import ru.pioneersystem.pioneer2.dao.UserDao;
import ru.pioneersystem.pioneer2.dao.exception.NotFoundDaoException;
import ru.pioneersystem.pioneer2.dao.impl.UserDaoImpl;
import ru.pioneersystem.pioneer2.model.Event;
import ru.pioneersystem.pioneer2.model.User;
import ru.pioneersystem.pioneer2.service.DictionaryService;
import ru.pioneersystem.pioneer2.service.EventService;
import ru.pioneersystem.pioneer2.service.SessionListener;
import ru.pioneersystem.pioneer2.service.UserService;
import ru.pioneersystem.pioneer2.service.exception.PasswordException;
import ru.pioneersystem.pioneer2.service.exception.RestrictionException;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.service.CurrentUser;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service("userService")
public class UserServiceImpl implements UserService {
    private EventService eventService;
    private UserDao userDao;
    private CompanyDao companyDao;
    private DictionaryService dictionaryService;
    private CurrentUser currentUser;
    private LocaleBean localeBean;
    private MessageSource messageSource;
    private SessionListener sessionListener;

    @Autowired
    public UserServiceImpl(EventService eventService, UserDao userDao, CompanyDao companyDao,
                           DictionaryService dictionaryService, CurrentUser currentUser, LocaleBean localeBean,
                           MessageSource messageSource, SessionListener sessionListener) {
        this.eventService = eventService;
        this.userDao = userDao;
        this.companyDao = companyDao;
        this.dictionaryService = dictionaryService;
        this.currentUser = currentUser;
        this.localeBean = localeBean;
        this.messageSource = messageSource;
        this.sessionListener = sessionListener;
    }

    // TODO: 25.07.2017 Дыра. Метод не должен быть доступен из вне
    @Override
    public User getUserWithCompany(int userId) throws ServiceException {
        try {
            User user = userDao.getWithCompany(userId);
            String stateName = dictionaryService.getLocalizedStateName(user.getState(), localeBean.getLocale());
            if (stateName != null) {
                user.setStateName(stateName);
            }
            return user;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.user.userWithCompanyNotLoaded", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), userId);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public List<User> getUserList() throws ServiceException {
        try {
            List<User> users = userDao.getList(currentUser.getUser().getCompanyId());
            for (User user : users) {
                String stateName = dictionaryService.getLocalizedStateName(user.getState(), localeBean.getLocale());
                if (stateName != null) {
                    user.setStateName(stateName);
                }
            }
            return users;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.user.NotLoadedList", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public List<User> getUserList(int groupId) throws ServiceException {
        try {
            return userDao.getList(groupId, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.user.usersInGroupNotLoaded", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), groupId);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public Map<String, Integer> getUserMap() throws ServiceException {
        Map<String, Integer> users = new LinkedHashMap<>();
        for (User user : getUserList()) {
            users.put(user.getName(), user.getId());
        }
        return users;
    }

    @Override
    public User getNewUser() {
        User user = new User();
        user.setLinkGroups(new ArrayList<>());
        user.setCreateFlag(true);
        return user;
    }

    @Override
    public User getUser(int userId) throws ServiceException {
        try {
            User user = userDao.get(userId, currentUser.getUser().getCompanyId());
            String stateName = dictionaryService.getLocalizedStateName(user.getState(), localeBean.getLocale());
            if (stateName != null) {
                user.setStateName(stateName);
            }
            user.setCreateFlag(false);
            eventService.logEvent(Event.Type.USER_GETED, userId);
            return user;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.user.NotLoaded", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), userId);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void saveUser(User user) throws ServiceException {
        try {
            user.setLogin(user.getLogin().trim());
            user.setEmail(user.getEmail().trim());

            if (user.isCreateFlag()) {
                int currentUserCount = userDao.getCount(currentUser.getUser().getCompanyId(), User.State.ACTIVE);
                int maxUserCount = companyDao.getMaxUserCount(currentUser.getUser().getCompanyId());

                if (currentUserCount >= maxUserCount) {
                    String mess = messageSource.getMessage("error.user.maxUserLimit", new Object[]{maxUserCount}, localeBean.getLocale());
                    eventService.logError(mess, null);
                    throw new RestrictionException(mess);
                }
                int userId = userDao.create(user, currentUser.getUser().getCompanyId());
                eventService.logEvent(Event.Type.USER_CREATED, userId);
            } else {
                userDao.update(user, currentUser.getUser().getCompanyId());
                eventService.logEvent(Event.Type.USER_CHANGED, user.getId());
            }
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.user.NotSaved", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), user.getId());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void lockUser(int userId) throws ServiceException {
        try {
            userDao.setState(User.State.LOCKED, userId, currentUser.getUser().getCompanyId());
            eventService.logEvent(Event.Type.USER_LOCKED, userId);
            sessionListener.invalidateUserSessions(userId);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.user.NotLocked", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), userId);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void unlockUser(int userId) throws ServiceException {
        try {
            int currentUserCount = userDao.getCount(currentUser.getUser().getCompanyId(), User.State.ACTIVE);
            int maxUserCount = companyDao.getMaxUserCount(currentUser.getUser().getCompanyId());

            if (currentUserCount >= maxUserCount) {
                String mess = messageSource.getMessage("error.user.maxUserLimit", new Object[]{maxUserCount}, localeBean.getLocale());
                eventService.logError(mess, null);
                throw new RestrictionException(mess);
            }
            userDao.setState(User.State.ACTIVE, userId, currentUser.getUser().getCompanyId());
            eventService.logEvent(Event.Type.USER_UNLOCKED, userId);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.user.NotUnlocked", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), userId);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void setUserPass(int userId, String newPass) throws ServiceException {
        try {
            userDao.savePass(userId, toHash(userId, newPass));
            eventService.logEvent(Event.Type.USER_PASS_SETUP, userId);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.user.passNotSetup", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), userId);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void changeUserPass(String login, String oldPass, String newPass) throws ServiceException {
        try {
            int userId = checkLoginAndPass(login, oldPass);
            userDao.savePass(userId, toHash(userId, newPass));
            eventService.logEvent(Event.Type.USER_PASS_CHANGED, userId);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.user.passNotChanged", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage());
            throw new ServiceException(mess, e);
        } catch (PasswordException e) {
            String mess = messageSource.getMessage("error.oldPass.not.valid", null, localeBean.getLocale());
            eventService.logError(mess, null);
            throw new PasswordException(mess);
        } catch (ServiceException e) {
            String mess = messageSource.getMessage("error.user.passNotChanged", null, localeBean.getLocale());
            eventService.logError(mess + ": " + e.getMessage(), null);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public int checkLoginAndPass(String login, String pass) throws ServiceException {
        Map<String, Object> userIdAndPass;
        try {
            userIdAndPass = userDao.getUserIdAndPass(login);
        } catch (NotFoundDaoException e) {
            String mess = messageSource.getMessage("error.user.userNameOrPassNotValid", null, localeBean.getLocale());
//            eventService.logError(mess, null);
            throw new PasswordException(mess);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.loginAndPass.not.checked", null, localeBean.getLocale());
//            eventService.logError(mess, e.getMessage());
            throw new ServiceException(mess, e);
        }

        int userId = (int) userIdAndPass.get(UserDaoImpl.USER_ID);
        String storedPass = (String) userIdAndPass.get(UserDaoImpl.PASS);

        if (toHash(userId, pass).equals(storedPass)) {
            return userId;
        } else {
            String mess = messageSource.getMessage("error.user.userNameOrPassNotValid", null, localeBean.getLocale());
//            eventService.logError(mess, null);
            throw new PasswordException(mess);
        }
    }

    private String toHash(int userId, String pass) {
        return DigestUtils.sha256Hex(userId + "Вся#соль" + pass + "И%сахара$чуть-чуть");
    }
}
