package ru.pioneersystem.pioneer2.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.dao.RoleDao;
import ru.pioneersystem.pioneer2.model.Event;
import ru.pioneersystem.pioneer2.model.Role;
import ru.pioneersystem.pioneer2.service.DictionaryService;
import ru.pioneersystem.pioneer2.service.EventService;
import ru.pioneersystem.pioneer2.service.RoleService;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.service.CurrentUser;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service("roleService")
public class RoleServiceImpl implements RoleService {
    private EventService eventService;
    private RoleDao roleDao;
    private DictionaryService dictionaryService;
    private CurrentUser currentUser;
    private LocaleBean localeBean;
    private MessageSource messageSource;

    @Autowired
    public RoleServiceImpl(EventService eventService, RoleDao roleDao, DictionaryService dictionaryService,
                           CurrentUser currentUser, LocaleBean localeBean, MessageSource messageSource) {
        this.eventService = eventService;
        this.roleDao = roleDao;
        this.dictionaryService = dictionaryService;
        this.currentUser = currentUser;
        this.localeBean = localeBean;
        this.messageSource = messageSource;
    }

    @Override
    public List<Role> getRoleList() throws ServiceException {
        try {
            List<Role> roles = roleDao.getList(currentUser.getUser().getCompanyId());
            for (Role role : roles) {
                String roleName = dictionaryService.getLocalizedRoleName(role.getId(), localeBean.getLocale());
                if (roleName != null) {
                    role.setName(roleName);
                }
            }
            return roles;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.role.NotLoadedList", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage());
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
            eventService.logError(mess, e.getMessage());
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
            Role role = roleDao.get(roleId, currentUser.getUser().getCompanyId());
            String roleName = dictionaryService.getLocalizedRoleName(role.getId(), localeBean.getLocale());
            if (roleName != null) {
                role.setName(roleName);
            }
            role.setCreateFlag(false);
            eventService.logEvent(Event.Type.ROLE_GETED, roleId);
            return role;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.role.NotLoaded", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), roleId);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void saveRole(Role role) throws ServiceException {
        try {
            if (role.isCreateFlag()) {
                int roleId = roleDao.create(role, currentUser.getUser().getCompanyId());
                eventService.logEvent(Event.Type.ROLE_CREATED, roleId);
            } else {
                roleDao.update(role, currentUser.getUser().getCompanyId());
                eventService.logEvent(Event.Type.ROLE_CHANGED, role.getId());
            }
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.role.NotSaved", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), role.getId());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void deleteRole(int roleId) throws ServiceException {
        // TODO: 28.02.2017 Проверка на удаление системной роли плюс еще какая-нибудь проверка
        try {
            roleDao.delete(roleId, currentUser.getUser().getCompanyId());
            eventService.logEvent(Event.Type.ROLE_DELETED, roleId);
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.role.NotDeleted", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), roleId);
            throw new ServiceException(mess, e);
        }
    }
}
