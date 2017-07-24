package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.model.Role;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;
import java.util.Map;

public interface RoleService {

    Role getRole(int roleId) throws ServiceException;

    List<Role> getRoleList() throws ServiceException;

    Map<String, Integer> getRoleMap() throws ServiceException;

    void createRole(Role role) throws ServiceException;

    void updateRole(Role role) throws ServiceException;

    void deleteRole(int roleId) throws ServiceException;
}