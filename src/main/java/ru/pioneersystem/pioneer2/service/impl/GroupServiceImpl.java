package ru.pioneersystem.pioneer2.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.pioneersystem.pioneer2.dao.GroupDao;
import ru.pioneersystem.pioneer2.dao.PartDao;
import ru.pioneersystem.pioneer2.dao.RouteDao;
import ru.pioneersystem.pioneer2.model.Group;
import ru.pioneersystem.pioneer2.service.GroupService;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.view.CurrentUser;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service("groupService")
public class GroupServiceImpl implements GroupService {
    private Logger log = LoggerFactory.getLogger(GroupServiceImpl.class);

    private GroupDao groupDao;
    private PartDao partDao;
    private RouteDao routeDao;
    private CurrentUser currentUser;
    private LocaleBean localeBean;
    private MessageSource messageSource;

    @Autowired
    public GroupServiceImpl(GroupDao groupDao, PartDao partDao, RouteDao routeDao, CurrentUser currentUser,
                            LocaleBean localeBean, MessageSource messageSource) {
        this.groupDao = groupDao;
        this.partDao = partDao;
        this.routeDao = routeDao;
        this.currentUser = currentUser;
        this.localeBean = localeBean;
        this.messageSource = messageSource;
    }

    @Override
    public List<Group> getGroupList() throws ServiceException {
        try {
            return groupDao.getList(currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.group.NotLoadedList", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public Map<String, Integer> getGroupMap() throws ServiceException {
        Map<String, Integer> groups = new LinkedHashMap<>();
        for (Group group : getGroupList()) {
            groups.put(group.getName(), group.getId());
        }
        return groups;
    }

    @Override
    public Map<String, Group> getPointMap() throws ServiceException {
        try {
            return groupDao.getRouteGroup(currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.group.routePointsNotLoaded", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public Map<String, Integer> getUserPublishMap() throws ServiceException {
        try {
            return groupDao.getUserPublishGroup(currentUser.getUser().getId(), currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.group.userPublishGroupNotLoaded", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public Map<String, Integer> getUserCreateGroupsMap() throws ServiceException {
        try {
            return groupDao.getUserCreateGroup(currentUser.getUser().getId(), currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.group.userCreateGroupNotLoaded", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public Map<Integer, Map<Integer, Integer>> getUserRolesGroupActivityMap() throws ServiceException {
        try {
            return groupDao.getUserRolesGroupActivity(currentUser.getUser().getId(), currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.group.userRolesGroupActivity", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public List<String> groupsWithRole(int roleId) throws ServiceException {
        try {
            return groupDao.groupsWithRole(roleId, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.group.groupsWithRole", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public Group getNewGroup() {
        Group group = new Group();
        group.setLinkUsers(new LinkedList<>());
        group.setCreateFlag(true);
        return group;
    }

    @Override
    public Group getGroup(int groupId) throws ServiceException {
        try {
            Group group = groupDao.get(groupId, currentUser.getUser().getCompanyId());
            group.setCreateFlag(false);
            return group;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.group.NotLoaded", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void saveGroup(Group group) throws ServiceException {
        try {
            if (group.isCreateFlag()) {
                groupDao.create(group, currentUser.getUser().getCompanyId());
            } else {
                groupDao.update(group, currentUser.getUser().getCompanyId());
            }
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.group.NotSaved", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void deleteGroup(int groupId) throws ServiceException {
        // TODO: 28.02.2017 Проверка на удаление системной группы плюс еще какая-нибудь проверка
        // пример:
        // установить @Transactional(rollbackForClassName = DaoException.class)
        // после проверки выбрасывать RestrictionException("Нельзя удалять, пока используется в шаблоне")
        // в ManagedBean проверять, если DaoException - то выдавать сообщение из DaoException
        try {
            partDao.removeGroupRestriction(groupId, currentUser.getUser().getCompanyId());
            routeDao.removeGroupRestriction(groupId, currentUser.getUser().getCompanyId());
            groupDao.delete(groupId, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.group.NotDeleted", null, localeBean.getLocale());
            log.error(mess, e);
            throw new ServiceException(mess, e);
        }
    }
}
