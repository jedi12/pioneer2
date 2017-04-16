package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.dao.exception.RestrictException;
import ru.pioneersystem.pioneer2.model.Role;
import ru.pioneersystem.pioneer2.model.Status;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;
import java.util.Locale;

public interface RoleService {
    Role getRole(int id, Locale locale) throws ServiceException;

    List<Role> getRoleList(int companyId, Locale locale) throws ServiceException;

    void createRole(Role role, Status status, int companyId) throws ServiceException;

    void updateRole(Role role, Status status) throws ServiceException;

    void deleteRole(int id) throws ServiceException, RestrictException;
}