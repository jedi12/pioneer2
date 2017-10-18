package ru.pioneersystem.pioneer2.dao;

import org.springframework.dao.DataAccessException;
import ru.pioneersystem.pioneer2.model.Role;

import java.util.List;
import java.util.Map;

public interface RoleDao {

    List<Role> getSuperList() throws DataAccessException;

    List<Role> getAdminList(int companyId) throws DataAccessException;

    Map<Integer, Role> getUserRole(int userId, int companyId) throws DataAccessException;

    Role get(int roleId, int companyId) throws DataAccessException;

    int create(Role role, int companyId) throws DataAccessException;

    void update(Role role, int companyId) throws DataAccessException;

    void delete(int roleId, int companyId) throws DataAccessException;
}