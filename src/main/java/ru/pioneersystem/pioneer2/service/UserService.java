package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.model.User;
import ru.pioneersystem.pioneer2.service.exception.PasswordException;
import ru.pioneersystem.pioneer2.service.exception.RestrictionException;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;
import java.util.Map;

public interface UserService {

    User getUser(int userId) throws ServiceException;

    User getUserWithCompany(int userId) throws ServiceException;

    List<User> getUserList() throws ServiceException;

    Map<String, Integer> getUserMap() throws ServiceException;

    void createUser(User user) throws ServiceException, RestrictionException;

    void updateUser(User user) throws ServiceException;

    void lockUser(int userId) throws ServiceException;

    void unlockUser(int userId) throws ServiceException, RestrictionException;

    void setUserPass(int userId, String newPass) throws ServiceException;

    void changeUserPass(String login, String oldPass, String newPass) throws ServiceException, PasswordException;

    int getUserId(String login, String pass) throws ServiceException, PasswordException;
}