package ru.pioneersystem.pioneer2.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.dao.RoleDao;
import ru.pioneersystem.pioneer2.model.Role;
import ru.pioneersystem.pioneer2.service.RoleService;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;

@Service("roleService")
public class RoleServiceImpl implements RoleService {
    private Logger log = LoggerFactory.getLogger(RoleServiceImpl.class);

    private RoleDao roleDao;

    @Autowired
    public RoleServiceImpl(RoleDao roleDao) {
        this.roleDao = roleDao;
    }

    @Override
    public Role getRole(int id) throws ServiceException {
        try {
            return roleDao.get(id);
        } catch (DataAccessException e) {
            log.error("Can't get Role by id", e);
            throw new ServiceException("Can't get Role by id", e);
        }
    }

    @Override
    public List<Role> getRoleList(int companyId) throws ServiceException {
        try {
            return roleDao.getList(companyId);
        } catch (DataAccessException e) {
            log.error("Can't get list of Role", e);
            throw new ServiceException("Can't get list of Role", e);
        }
    }

    @Override
    public void createRole(Role role, int companyId) throws ServiceException {
        try {
            roleDao.create(role, companyId);
        } catch (DataAccessException e) {
            log.error("Can't create Role", e);
            throw new ServiceException("Can't create Role", e);
        }
    }

    @Override
    public void updateRole(Role role) throws ServiceException {
        try {
            roleDao.update(role);
        } catch (DataAccessException e) {
            log.error("Can't update Role", e);
            throw new ServiceException("Can't update Role", e);
        }
    }

    @Override
    public void deleteRole(int id) throws ServiceException {
        try {
            roleDao.delete(id);
        } catch (DataAccessException e) {
            log.error("Can't delete Role", e);
            throw new ServiceException("Can't delete Role", e);
        }
    }
}
