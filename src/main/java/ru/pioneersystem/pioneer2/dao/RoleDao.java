package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.Role;

import java.util.List;

public interface RoleDao {

    Role get(int roleId) throws DataAccessException;

    List<Role> getList(int companyId) throws DataAccessException;

    void create(Role role, int companyId) throws DataAccessException;

    void update(Role role) throws DataAccessException;

    void delete(int roleId) throws DataAccessException;
}