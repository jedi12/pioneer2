package ru.pioneersystem.pioneer2.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.dao.RoleDao;
import ru.pioneersystem.pioneer2.model.Role;
import ru.pioneersystem.pioneer2.model.Status;
import ru.pioneersystem.pioneer2.service.RoleService;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;
import java.util.Locale;

@Service("roleService")
public class RoleServiceImpl implements RoleService {
    private Logger log = LoggerFactory.getLogger(RoleServiceImpl.class);

    private RoleDao roleDao;
    private MessageSource messageSource;

    @Autowired
    public RoleServiceImpl(RoleDao roleDao, MessageSource messageSource) {
        this.roleDao = roleDao;
        this.messageSource = messageSource;
    }

    @Override
    public Role getRole(int id, Locale locale) throws ServiceException {
        try {
            return setRoleName(roleDao.get(id), locale);
        } catch (DataAccessException e) {
            log.error("Can't get Role by id", e);
            throw new ServiceException("Can't get Role by id", e);
        }
    }

    @Override
    public List<Role> getRoleList(int companyId, Locale locale) throws ServiceException {
        try {
            List<Role> roles = roleDao.getList(companyId);
            for (Role role : roles) {
                setRoleName(role, locale);
            }
            return roles;
        } catch (DataAccessException e) {
            log.error("Can't get list of Role", e);
            throw new ServiceException("Can't get list of Role", e);
        }
    }

    @Override
    public void createRole(Role role, Status status, int companyId) throws ServiceException {
        try {
            roleDao.create(role, status, companyId);
        } catch (DataAccessException e) {
            log.error("Can't create Role", e);
            throw new ServiceException("Can't create Role", e);
        }
    }

    @Override
    public void updateRole(Role role, Status status) throws ServiceException {
        try {
            roleDao.update(role, status);
        } catch (DataAccessException e) {
            log.error("Can't update Role", e);
            throw new ServiceException("Can't update Role", e);
        }
    }

    @Override
    public void deleteRole(int id) throws ServiceException {
        // TODO: 28.02.2017 Проверка на удаление системной роли плюс еще какая-нибудь проверка
        // пример:
        // установить @Transactional(rollbackForClassName = DaoException.class)
        // после проверки выбрасывать RestrictException("Нельзя удалять, пока используется в шаблоне")
        // в ManagedBean проверять, если DaoException - то выдавать сообщение из DaoException
        try {
            roleDao.delete(id);
        } catch (DataAccessException e) {
            log.error("Can't delete Role", e);
            throw new ServiceException("Can't delete Role", e);
        }
    }

    private Role setRoleName(Role role, Locale locale) {
        if (role.getState() == Role.State.SYSTEM) {
            switch (role.getId()) {
                case Role.Id.SUPER:
                    role.setName(messageSource.getMessage("role.name.super", null, locale));
                    break;
                case Role.Id.ADMIN:
                    role.setName(messageSource.getMessage("role.name.admin", null, locale));
                    break;
                case Role.Id.USER:
                    role.setName(messageSource.getMessage("role.name.user", null, locale));
                    break;
                case Role.Id.REZ1:
                    role.setName(messageSource.getMessage("role.name.rez1", null, locale));
                    break;
                case Role.Id.PUBLIC:
                    role.setName(messageSource.getMessage("role.name.public", null, locale));
                    break;
                case Role.Id.COMMENT:
                    role.setName(messageSource.getMessage("role.name.comment", null, locale));
                    break;
                case Role.Id.EDIT:
                    role.setName(messageSource.getMessage("role.name.edit", null, locale));
                    break;
                case Role.Id.REZ2:
                    role.setName(messageSource.getMessage("role.name.rez2", null, locale));
                    break;
                case Role.Id.CREATE:
                    role.setName(messageSource.getMessage("role.name.create", null, locale));
                    break;
                case Role.Id.ON_COORDINATION:
                    role.setName(messageSource.getMessage("role.name.coordinate", null, locale));
                    break;
                case Role.Id.ON_EXECUTION:
                    role.setName(messageSource.getMessage("role.name.execute", null, locale));
                    break;
                default:
                    role.setName("Unknown");
            }
        }
        return role;
    }
}
