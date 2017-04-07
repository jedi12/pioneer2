package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.model.User;
import ru.pioneersystem.pioneer2.service.exception.PasswordException;
import ru.pioneersystem.pioneer2.service.exception.RestrictionException;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;
import java.util.Locale;

public interface UserService {
    User getUser(int id, Locale locale) throws ServiceException;

    User getUserWithCompany(int id, Locale locale) throws ServiceException;

    List<User> getUserList(int companyId, Locale locale) throws ServiceException;

    void createUser(User user, int companyId) throws ServiceException, RestrictionException;

    void updateUser(User user) throws ServiceException;

    void lockUser(int id) throws ServiceException;

    void unlockUser(int id, int companyId) throws ServiceException, RestrictionException;

    void setUserPass(int id, String newPass) throws ServiceException;

    void changeUserPass(String login, String oldPass, String newPass) throws ServiceException, PasswordException;

    int getUserId(String login, String pass) throws ServiceException, PasswordException;
}