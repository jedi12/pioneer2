package ru.pioneersystem.pioneer2.service.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pioneersystem.pioneer2.dao.CompanyDao;
import ru.pioneersystem.pioneer2.dao.UserDao;
import ru.pioneersystem.pioneer2.dao.exception.NotFoundDaoException;
import ru.pioneersystem.pioneer2.dao.impl.UserDaoImpl;
import ru.pioneersystem.pioneer2.model.Event;
import ru.pioneersystem.pioneer2.model.Role;
import ru.pioneersystem.pioneer2.model.User;
import ru.pioneersystem.pioneer2.service.*;
import ru.pioneersystem.pioneer2.service.exception.PasswordException;
import ru.pioneersystem.pioneer2.service.exception.RestrictionException;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
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
    private GroupService groupService;
    private DictionaryService dictionaryService;
    private CurrentUser currentUser;
    private LocaleBean localeBean;
    private MessageSource messageSource;
    private SessionListener sessionListener;

    @Autowired
    public UserServiceImpl(EventService eventService, UserDao userDao, CompanyDao companyDao, GroupService groupService,
                           DictionaryService dictionaryService, CurrentUser currentUser, LocaleBean localeBean,
                           MessageSource messageSource, SessionListener sessionListener) {
        this.eventService = eventService;
        this.userDao = userDao;
        this.companyDao = companyDao;
        this.groupService = groupService;
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
            List<User> users;
            if (currentUser.isSuperRole()) {
                users = userDao.getSuperList();
            } else {
                users = userDao.getAdminList(currentUser.getUser().getCompanyId());
            }

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
    public List<User> getUsersInGroup(int groupId) throws ServiceException {
        try {
            return userDao.getInGroup(groupId, currentUser.getUser().getCompanyId());
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
        user.setCreateOwnGroup(true);
        user.setLinkGroups(new ArrayList<>());
        user.setCreateFlag(true);
        return user;
    }

    @Override
    public User getUser(User selectedUser) throws ServiceException {
        if (selectedUser == null) {
            String mess = messageSource.getMessage("error.user.NotSelected", null, localeBean.getLocale());
            throw new RestrictionException(mess);
        }

        try {
            int companyId;
            if (currentUser.isSuperRole()) {
                companyId = selectedUser.getCompanyId();
            } else {
                companyId = currentUser.getUser().getCompanyId();
            }

            User user = userDao.get(selectedUser.getId(), companyId);
            String stateName = dictionaryService.getLocalizedStateName(user.getState(), localeBean.getLocale());
            if (stateName != null) {
                user.setStateName(stateName);
            }
            user.setCreateFlag(false);
            eventService.logEvent(Event.Type.USER_GETED, selectedUser.getId());
            return user;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.user.NotLoaded", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), selectedUser.getId());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public User getUserById(int userId) throws ServiceException {
        User user = new User();
        user.setId(userId);
        return getUser(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUser(User user) throws ServiceException {
        try {
            user.setLogin(user.getLogin().trim());
            user.setEmail(user.getEmail().trim());

            if (user.isCreateFlag()) {
                int currentUserCount = userDao.getCount(currentUser.getUser().getCompanyId(), User.State.ACTIVE);
                int maxUserCount = companyDao.getMaxUserCount(currentUser.getUser().getCompanyId());
                if (currentUserCount >= maxUserCount) {
                    String mess = messageSource.getMessage("error.user.maxUserLimit", new Object[]{maxUserCount}, localeBean.getLocale());
                    eventService.logEvent(Event.Type.USER_RESTRICTION_ACHIEVED, 0, mess);
                    throw new RestrictionException(mess);
                }

                if (userDao.getCountByLogin(user.getLogin()) > 0) {
                    String mess = messageSource.getMessage("error.user.loginAlreadyExists", new Object[]{user.getLogin()}, localeBean.getLocale());
                    eventService.logEvent(Event.Type.USER_RESTRICTION_ACHIEVED, user.getId(), mess);
                    throw new RestrictionException(mess);
                }

                if (userDao.getCountByEmail(user.getEmail()) > 0) {
                    String mess = messageSource.getMessage("error.user.emailAlreadyExists", new Object[]{user.getEmail()}, localeBean.getLocale());
                    eventService.logEvent(Event.Type.USER_RESTRICTION_ACHIEVED, user.getId(), mess);
                    throw new RestrictionException(mess);
                }

                int userId = userDao.create(user, currentUser.getUser().getCompanyId());
                // TODO: 22.10.2017 Сделать логирование событий в той же транзакции, а логирование ошибок - в отдельной транзакции
                eventService.logEvent(Event.Type.USER_CREATED, userId);

                if (user.isCreateOwnGroup()) {
                    groupService.createGroupWithUser(user.getName(), Role.Id.CREATE, userId, currentUser.getUser().getCompanyId());
                }
            } else {
                userDao.update(user, currentUser.getUser().getCompanyId());
                eventService.logEvent(Event.Type.USER_CHANGED, user.getId());
            }
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.user.NotSaved", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), user.getId());
            throw new ServiceException(mess, e);
        } catch (ServiceException e) {
            String mess = messageSource.getMessage("error.user.NotSaved", null, localeBean.getLocale());
            eventService.logError(mess + ": " + e.getMessage(), null);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void lockUser(User user) throws ServiceException {
        if (user == null) {
            String mess = messageSource.getMessage("error.user.NotSelected", null, localeBean.getLocale());
            throw new RestrictionException(mess);
        }

        if (user.getState() == User.State.SYSTEM) {
            String mess = messageSource.getMessage("error.operation.NotAllowed", null, localeBean.getLocale());
            throw new RestrictionException(mess);
        }

        if (user.getState() == User.State.LOCKED) {
            return;
        }

        try {
            int companyId;
            if (currentUser.isSuperRole()) {
                companyId = user.getCompanyId();
            } else {
                companyId = currentUser.getUser().getCompanyId();
            }
            userDao.setState(User.State.LOCKED, user.getId(), companyId);
            eventService.logEvent(Event.Type.USER_LOCKED, user.getId());
            sessionListener.invalidateUserSessions(user.getId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.user.NotLocked", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), user.getId());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void unlockUser(User user) throws ServiceException {
        if (user == null) {
            String mess = messageSource.getMessage("error.user.NotSelected", null, localeBean.getLocale());
            throw new RestrictionException(mess);
        }

        if (user.getState() == User.State.SYSTEM) {
            String mess = messageSource.getMessage("error.operation.NotAllowed", null, localeBean.getLocale());
            throw new RestrictionException(mess);
        }

        if (user.getState() == User.State.ACTIVE) {
            return;
        }

        try {
            int companyId;
            if (currentUser.isSuperRole()) {
                companyId = user.getCompanyId();
            } else {
                companyId = currentUser.getUser().getCompanyId();
            }

            int currentUserCount = userDao.getCount(companyId, User.State.ACTIVE);
            int maxUserCount = companyDao.getMaxUserCount(companyId);

            if (currentUserCount >= maxUserCount) {
                String mess = messageSource.getMessage("error.user.maxUserLimit", new Object[]{maxUserCount}, localeBean.getLocale());
                eventService.logEvent(Event.Type.USER_RESTRICTION_ACHIEVED, user.getId(), mess);
                throw new RestrictionException(mess);
            }
            userDao.setState(User.State.ACTIVE, user.getId(), companyId);
            eventService.logEvent(Event.Type.USER_UNLOCKED, user.getId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.user.NotUnlocked", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), user.getId());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void setUserPass(User user, String newPass) throws ServiceException {
        if (user == null) {
            String mess = messageSource.getMessage("error.user.NotSelected", null, localeBean.getLocale());
            throw new RestrictionException(mess);
        }

        if (user.getId() == 0) {
            String mess = messageSource.getMessage("error.operation.NotAllowed", null, localeBean.getLocale());
            throw new RestrictionException(mess);
        }

        try {
            userDao.savePass(user.getId(), toHash(user.getId(), newPass));
            eventService.logEvent(Event.Type.USER_PASS_SETUP, user.getId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.user.passNotSetup", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), user.getId());
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int createAdminUser(String userName, String userLogin, String userEmail, int companyId) throws ServiceException {
        try {
            User user = getNewUser();
            user.setName(userName);
            user.setLogin(userLogin);
            user.setEmail(userEmail);
            return userDao.create(user, companyId);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.user.adminNotCreated", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage());
            throw new ServiceException(mess, e);
        }
    }
}
