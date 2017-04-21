package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.Role;

import java.util.List;

public interface RoleDao {

    Role get(int id) throws DataAccessException;

    List<Role> getList(int company) throws DataAccessException;

    void create(Role role, int company) throws DataAccessException;

    void update(Role role) throws DataAccessException;

    void delete(int id) throws DataAccessException;
}