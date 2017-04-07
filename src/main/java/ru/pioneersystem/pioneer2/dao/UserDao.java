package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.User;

import java.util.List;
import java.util.Map;

public interface UserDao {

    User get(int id) throws DataAccessException;

    User getWithCompany(int id) throws DataAccessException;

    List<User> getList(int company) throws DataAccessException;

    void create(User user, int company) throws DataAccessException;

    void update(User user) throws DataAccessException;

    void lock(int id) throws DataAccessException;

    void unlock(int id) throws DataAccessException;

    void savePass(int id, String passHash) throws DataAccessException;

    Map<String, Object> getUserIdAndPass(String login) throws DataAccessException;

    int getCount(int company, int state) throws DataAccessException;
}