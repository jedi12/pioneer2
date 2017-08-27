package ru.pioneersystem.pioneer2.service.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.dao.CompanyDao;
import ru.pioneersystem.pioneer2.dao.UserDao;
import ru.pioneersystem.pioneer2.dao.impl.UserDaoImpl;
import ru.pioneersystem.pioneer2.model.User;
import ru.pioneersystem.pioneer2.service.DictionaryService;
import ru.pioneersystem.pioneer2.service.SessionListener;
import ru.pioneersystem.pioneer2.service.UserService;
import ru.pioneersystem.pioneer2.service.exception.PasswordException;
import ru.pioneersystem.pioneer2.service.exception.RestrictionException;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.view.CurrentUser;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service("userService")
public class UserServiceImpl implements UserService {
    private Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private UserDao userDao;
    private CompanyDao companyDao;
    private DictionaryService dictionaryService;
    private CurrentUser currentUser;
    private LocaleBean localeBean;
    private MessageSource messageSource;
    private SessionListener sessionListener;

    @Autowired
    public UserServiceImpl(UserDao userDao, CompanyDao companyDao, DictionaryService dictionaryService,
                           CurrentUser currentUser, LocaleBean localeBean, MessageSource messageSource,
                           SessionListener sessionListener) {
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
            String stateName = dictionaryService.getLocalizedStateName(user.getState());
            if (stateName != null) {
                user.setStateName(stateName);
            }
            return user;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.user.userWithCompanyNotLoaded", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public List<User> getUserList() throws ServiceException {
        try {
            List<User> users = userDao.getList(currentUser.getUser().getCompanyId());
            for (User user : users) {
                String stateName = dictionaryService.getLocalizedStateName(user.getState());
                if (stateName != null) {
                    user.setStateName(stateName);
                }
            }
            return users;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.user.NotLoadedList", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public List<User> getUserList(int groupId) throws ServiceException {
        try {
            return userDao.getList(groupId, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.user.usersInGroupNotLoaded", null, localeBean.getLocale());
            log.error(mess, e);
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
        user.setCreateFlag(true);
        return user;
    }

    @Override
    public User getUser(int userId) throws ServiceException {
        try {
            User user = userDao.get(userId, currentUser.getUser().getCompanyId());
            String stateName = dictionaryService.getLocalizedStateName(user.getState());
            if (stateName != null) {
                user.setStateName(stateName);
            }
            user.setCreateFlag(false);
            return user;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.user.NotLoaded", null, localeBean.getLocale());
            log.error(mess, e);
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
                    String mess = messageSource.getMessage("error.user.maxUserLimit", null, localeBean.getLocale());
                    log.error(mess);
                    throw new RestrictionException(mess);
                }
                userDao.create(user, currentUser.getUser().getCompanyId());
            } else {
                userDao.update(user, currentUser.getUser().getCompanyId());
            }
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.user.NotSaved", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void lockUser(int userId) throws ServiceException {
        try {
            userDao.setState(User.State.LOCKED, userId, currentUser.getUser().getCompanyId());
            sessionListener.invalidateUserSessions(userId);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.user.NotLocked", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void unlockUser(int userId) throws ServiceException {
        try {
            int currentUserCount = userDao.getCount(currentUser.getUser().getCompanyId(), User.State.ACTIVE);
            int maxUserCount = companyDao.getMaxUserCount(currentUser.getUser().getCompanyId());

            if (currentUserCount >= maxUserCount) {
                String mess = messageSource.getMessage("error.user.maxUserLimit", null, localeBean.getLocale());
                log.error(mess);
                throw new RestrictionException(mess);
            }
            userDao.setState(User.State.ACTIVE, userId, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.user.NotUnlocked", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void setUserPass(int userId, String newPass) throws ServiceException {
        try {
            userDao.savePass(userId, toHash(userId, newPass));
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.user.passNotSetup", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void changeUserPass(String login, String oldPass, String newPass) throws ServiceException {
        try {
            int loginId = checkLoginAndPass(login, oldPass);
            userDao.savePass(loginId, toHash(loginId, newPass));
        } catch (PasswordException e) {
            String mess = messageSource.getMessage("error.user.oldPassNotValid", null, localeBean.getLocale());
            log.error(mess, e);
            throw new PasswordException(mess);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.user.passNotChanged", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public int checkLoginAndPass(String login, String pass) throws ServiceException {
        Map<String, Object> userIdAndPass;
        try {
            userIdAndPass = userDao.getUserIdAndPass(login);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.user.userNameAndPassNotLoaded", null, localeBean.getLocale());
            log.error(mess, e);
            throw new PasswordException(mess);
        }

        int loginId = (int) userIdAndPass.get(UserDaoImpl.USER_ID);
        String storedPass = (String) userIdAndPass.get(UserDaoImpl.PASS);

        if (toHash(loginId, pass).equals(storedPass)) {
            return loginId;
        } else {
            String mess = messageSource.getMessage("error.user.userNameOrPassNotValid", null, localeBean.getLocale());
            log.error(mess);
            throw new PasswordException(mess);
        }
    }

    private String toHash(int userId, String pass) {
        return DigestUtils.sha256Hex(userId + "Вся#соль" + pass + "И%сахара$чуть-чуть");
    }
}
