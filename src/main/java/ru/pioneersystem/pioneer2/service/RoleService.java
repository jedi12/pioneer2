package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.model.Role;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;
import java.util.Map;

public interface RoleService {

    List<Role> getRoleList() throws ServiceException;

    Map<String, Integer> getRoleMap() throws ServiceException;

    Map<Integer, Role> getUserRoleMap() throws ServiceException;

    Role getNewRole();

    Role getRole(Role selectedRole) throws ServiceException;

    void saveRole(Role role) throws ServiceException;

    void deleteRole(Role role) throws ServiceException;

    int createExampleRole(int roleExample, int companyId) throws ServiceException;
}