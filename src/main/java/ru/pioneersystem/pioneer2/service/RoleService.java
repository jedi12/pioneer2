package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.dao.exception.RestrictException;
import ru.pioneersystem.pioneer2.model.Role;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;

public interface RoleService {
    Role getRole(int id) throws ServiceException;

    List<Role> getRoleList() throws ServiceException;

    void createRole(Role role) throws ServiceException;

    void updateRole(Role role) throws ServiceException;

    void deleteRole(int id) throws ServiceException, RestrictException;
}