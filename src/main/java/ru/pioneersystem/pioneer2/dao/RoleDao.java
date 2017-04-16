package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.Role;
import ru.pioneersystem.pioneer2.model.Status;

import java.util.List;

public interface RoleDao {

    Role get(int id) throws DataAccessException;

    List<Role> getList(int company) throws DataAccessException;

    void create(Role role, Status status, int company) throws DataAccessException;

    void update(Role role, Status status) throws DataAccessException;

    void delete(int id) throws DataAccessException;
}