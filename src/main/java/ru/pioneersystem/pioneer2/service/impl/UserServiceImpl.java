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
import ru.pioneersystem.pioneer2.service.SessionListener;
import ru.pioneersystem.pioneer2.service.UserService;
import ru.pioneersystem.pioneer2.service.exception.PasswordException;
import ru.pioneersystem.pioneer2.service.exception.RestrictionException;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service("userService")
public class UserServiceImpl implements UserService {
    private Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final int USER_STATUS_LOCKED = 0;
    private static final int USER_STATUS_ACTIVE = 1;

    private UserDao userDao;
    private CompanyDao companyDao;
    private MessageSource messageSource;
    private SessionListener sessionListener;

    @Autowired
    public UserServiceImpl(UserDao userDao, CompanyDao companyDao, MessageSource messageSource, SessionListener sessionListener) {
        this.userDao = userDao;
        this.messageSource = messageSource;
        this.sessionListener = sessionListener;
        this.companyDao = companyDao;
    }

    @Override
    public User getUser(int id, Locale locale) throws ServiceException {
        try {
            return setUserStateName(userDao.get(id), locale);
        } catch (DataAccessException e) {
            log.error("Can't get User by id", e);
            throw new ServiceException("Can't get User by id", e);
        }
    }

    @Override
    public User getUserWithCompany(int id, Locale locale) throws ServiceException {
        try {
            return setUserStateName(userDao.getWithCompany(id), locale);
        } catch (DataAccessException e) {
            log.error("Can't get User by id", e);
            throw new ServiceException("Can't get User by id", e);
        }
    }

    @Override
    public List<User> getUserList(int companyId, Locale locale) throws ServiceException {
        try {
            List<User> users = userDao.getList(companyId);
            for (User user : users) {
                setUserStateName(user, locale);
            }
            return users;
        } catch (DataAccessException e) {
            log.error("Can't get list of User", e);
            throw new ServiceException("Can't get list of User", e);
        }
    }

    @Override
    public void createUser(User user, int companyId) throws ServiceException, RestrictionException {
        try {
            int currentUserCount = userDao.getCount(companyId, USER_STATUS_ACTIVE);
            int maxUserCount = companyDao.getMaxUserCount(companyId);

            if (currentUserCount >= maxUserCount) {
                throw new RestrictionException("License max users restriction");
            }
            userDao.create(user, companyId);
        } catch (DataAccessException e) {
            log.error("Can't create User", e);
            throw new ServiceException("Can't create User", e);
        }
    }

    @Override
    public void updateUser(User user) throws ServiceException {
        try {
            userDao.update(user);
        } catch (DataAccessException e) {
            log.error("Can't update User", e);
            throw new ServiceException("Can't update User", e);
        }
    }

    @Override
    public void lockUser(int id) throws ServiceException {
        try {
            userDao.lock(id);
            sessionListener.invalidateUserSessions(id);
        } catch (DataAccessException e) {
            log.error("Can't lock User", e);
            throw new ServiceException("Can't lock User", e);
        }
    }

    @Override
    public void unlockUser(int id, int companyId) throws ServiceException, RestrictionException {
        try {
            int currentUserCount = userDao.getCount(companyId, USER_STATUS_ACTIVE);
            int maxUserCount = companyDao.getMaxUserCount(companyId);

            if (currentUserCount >= maxUserCount) {
                throw new RestrictionException("License max users restriction");
            }
            userDao.unlock(id);
        } catch (DataAccessException e) {
            log.error("Can't unlock User", e);
            throw new ServiceException("Can't unlock User", e);
        }
    }

    @Override
    public void setUserPass(int id, String newPass) throws ServiceException {
        try {
            userDao.savePass(id, toHash(id, newPass));
        } catch (DataAccessException e) {
            log.error("Can't set User's pass", e);
            throw new ServiceException("Can't set User's pass", e);
        }
    }

    @Override
    public void changeUserPass(String login, String oldPass, String newPass) throws ServiceException, PasswordException {
        int loginId = getUserId(login, oldPass);
        try {
            userDao.savePass(loginId, toHash(loginId, newPass));
        } catch (DataAccessException e) {
            log.error("Can't change User's pass", e);
            throw new ServiceException("Can't change User's pass", e);
        }
    }

    @Override
    public int getUserId(String login, String pass) throws ServiceException, PasswordException {
        Map<String, Object> userIdAndPass;
        try {
            userIdAndPass = userDao.getUserIdAndPass(login);
        } catch (DataAccessException e) {
            log.error("Can't get User's Id", e);
            throw new ServiceException("Can't get User's Id", e);
        }

        if (userIdAndPass.isEmpty()) {
            throw new PasswordException("Login or password isn't valid");
        }

        int loginId = (int) userIdAndPass.get(UserDaoImpl.USER_ID);
        String storedPass = (String) userIdAndPass.get(UserDaoImpl.PASS);

        if (toHash(loginId, pass).equals(storedPass)) {
            return loginId;
        } else {
            throw new PasswordException("Login or password isn't valid");
        }
    }

    private User setUserStateName(User user, Locale locale) {
        switch (user.getState()) {
            case USER_STATUS_LOCKED:
                user.setStateName(messageSource.getMessage("status.locked", null, locale));
                break;
            case USER_STATUS_ACTIVE:
                user.setStateName(messageSource.getMessage("status.active", null, locale));
                break;
            default:
                user.setStateName("Unknown");
        }
        return user;
    }

    private String toHash(int id, String pass) {
        return DigestUtils.sha256Hex(id + "Вся#соль" + pass + "И%сахара$чуть-чуть");
    }
}