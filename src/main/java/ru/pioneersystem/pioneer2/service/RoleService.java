package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.model.Role;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;
import java.util.Map;

public interface RoleService {

    List<Role> getRoleList() throws ServiceException;

    Map<String, Integer> getRoleMap() throws ServiceException;

    Role getNewRole();

    Role getRole(int roleId) throws ServiceException;

    void saveRole(Role role) throws ServiceException;

    void deleteRole(int roleId) throws ServiceException;
}