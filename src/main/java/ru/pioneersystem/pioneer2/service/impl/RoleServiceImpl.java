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
    private LocaleBean localeBean;
    private CurrentUser currentUser;
    private MessageSource messageSource;

    @Autowired
    public RoleServiceImpl(RoleDao roleDao, LocaleBean localeBean, CurrentUser currentUser, MessageSource messageSource) {
        this.roleDao = roleDao;
        this.localeBean = localeBean;
        this.currentUser = currentUser;
        this.messageSource = messageSource;
    }

    @Override
    public Role getRole(int id) throws ServiceException {
        try {
            return setLocalizedRoleName(roleDao.get(id));
        } catch (DataAccessException e) {
            log.error("Can't get Role by id", e);
            throw new ServiceException("Can't get Role by id", e);
        }
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
            log.error("Can't get list of Role", e);
            throw new ServiceException("Can't get list of Role", e);
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
    public void createRole(Role role) throws ServiceException {
        try {
            roleDao.create(role, currentUser.getUser().getCompanyId());
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
