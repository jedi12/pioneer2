package ru.pioneersystem.pioneer2.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.dao.RoleDao;
import ru.pioneersystem.pioneer2.model.Role;
import ru.pioneersystem.pioneer2.service.RoleService;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.view.CurrentUser;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service("roleService")
public class RoleServiceImpl implements RoleService {
    private Logger log = LoggerFactory.getLogger(RoleServiceImpl.class);

    private RoleDao roleDao;
    private CurrentUser currentUser;
    private LocaleBean localeBean;
    private MessageSource messageSource;

    @Autowired
    public RoleServiceImpl(RoleDao roleDao, CurrentUser currentUser, LocaleBean localeBean, MessageSource messageSource) {
        this.roleDao = roleDao;
        this.currentUser = currentUser;
        this.localeBean = localeBean;
        this.messageSource = messageSource;
    }

    @Override
    public List<Role> getRoleList() throws ServiceException {
        try {
            List<Role> roles = roleDao.getList(currentUser.getUser().getCompanyId());
            for (Role role : roles) {
                setLocalizedRoleName(role);
            }
            return roles;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.role.NotLoadedList", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public Map<String, Integer> getRoleMap() throws ServiceException {
        Map<String, Integer> roles = new LinkedHashMap<>();
        for (Role roleList : getRoleList()) {
            roles.put(roleList.getName(), roleList.getId());
        }
        return roles;
    }

    @Override
    public Map<Integer, Role> getUserRoleMap() throws ServiceException {
        try {
            return roleDao.getUserRole(currentUser.getUser().getId(), currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.role.userRoleNotLoaded", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public Role getNewRole() {
        Role role = new Role();
        role.setCreateFlag(true);
        return role;
    }

    @Override
    public Role getRole(int roleId) throws ServiceException {
        try {
            Role role = setLocalizedRoleName(roleDao.get(roleId, currentUser.getUser().getCompanyId()));
            role.setCreateFlag(false);
            return role;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.role.NotLoaded", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void saveRole(Role role) throws ServiceException {
        try {
            if (role.isCreateFlag()) {
                roleDao.create(role, currentUser.getUser().getCompanyId());
            } else {
                roleDao.update(role, currentUser.getUser().getCompanyId());
            }
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.role.NotSaved", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void deleteRole(int roleId) throws ServiceException {
        // TODO: 28.02.2017 Проверка на удаление системной роли плюс еще какая-нибудь проверка
        // пример:
        // установить @Transactional(rollbackForClassName = DaoException.class)
        // после проверки выбрасывать RestrictionException("Нельзя удалять, пока используется в шаблоне")
        // в ManagedBean проверять, если DaoException - то выдавать сообщение из DaoException
        try {
            roleDao.delete(roleId, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.role.NotDeleted", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    private Role setLocalizedRoleName(Role role) {
        if (role.getState() == Role.State.SYSTEM) {
            switch (role.getId()) {
                case Role.Id.SUPER:
                    role.setName(messageSource.getMessage("role.name.super", null, localeBean.getLocale()));
                    break;
                case Role.Id.ADMIN:
                    role.setName(messageSource.getMessage("role.name.admin", null, localeBean.getLocale()));
                    break;
                case Role.Id.USER:
                    role.setName(messageSource.getMessage("role.name.user", null, localeBean.getLocale()));
                    break;
                case Role.Id.REZ1:
                    role.setName(messageSource.getMessage("role.name.rez1", null, localeBean.getLocale()));
                    break;
                case Role.Id.PUBLIC:
                    role.setName(messageSource.getMessage("role.name.public", null, localeBean.getLocale()));
                    break;
                case Role.Id.COMMENT:
                    role.setName(messageSource.getMessage("role.name.comment", null, localeBean.getLocale()));
                    break;
                case Role.Id.EDIT:
                    role.setName(messageSource.getMessage("role.name.edit", null, localeBean.getLocale()));
                    break;
                case Role.Id.REZ2:
                    role.setName(messageSource.getMessage("role.name.rez2", null, localeBean.getLocale()));
                    break;
                case Role.Id.CREATE:
                    role.setName(messageSource.getMessage("role.name.create", null, localeBean.getLocale()));
                    break;
                case Role.Id.ON_COORDINATION:
                    role.setName(messageSource.getMessage("role.name.coordinate", null, localeBean.getLocale()));
                    break;
                case Role.Id.ON_EXECUTION:
                    role.setName(messageSource.getMessage("role.name.execute", null, localeBean.getLocale()));
                    break;
                default:
                    role.setName("Unknown");
            }
        }
        return role;
    }
}
