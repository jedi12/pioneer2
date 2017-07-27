package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.model.User;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;
import java.util.Map;

public interface UserService {

    User getUserWithCompany(int userId) throws ServiceException;

    List<User> getUserList() throws ServiceException;

    Map<String, Integer> getUserMap() throws ServiceException;

    User getNewUser();

    User getUser(int userId) throws ServiceException;

    void saveUser(User user) throws ServiceException;

    void lockUser(int userId) throws ServiceException;

    void unlockUser(int userId) throws ServiceException;

    void setUserPass(int userId, String newPass) throws ServiceException;

    void changeUserPass(String login, String oldPass, String newPass) throws ServiceException;

    int checkLoginAndPass(String login, String pass) throws ServiceException;
}