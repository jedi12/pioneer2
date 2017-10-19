package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.User;

import java.util.List;
import java.util.Map;

public interface UserDao {

    List<User> getSuperList() throws DataAccessException;

    List<User> getAdminList(int companyId) throws DataAccessException;

    User get(int userId, int companyId) throws DataAccessException;

    User getWithCompany(int userId) throws DataAccessException;

    List<User> getInGroup(int groupId, int companyId) throws DataAccessException;

    int create(User user, int companyId) throws DataAccessException;

    void update(User user, int companyId) throws DataAccessException;

    void setState(int state, int userId, int companyId) throws DataAccessException;

    void savePass(int userId, String passHash) throws DataAccessException;

    Map<String, Object> getUserIdAndPass(String login) throws DataAccessException;

    int getCount(int companyId, int state) throws DataAccessException;

    int getCountByLogin(String login) throws DataAccessException;

    int getCountByEmail(String email) throws DataAccessException;
}