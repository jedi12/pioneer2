package ru.pioneersystem.pioneer2.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.pioneersystem.pioneer2.dao.GroupDao;
import ru.pioneersystem.pioneer2.dao.PartDao;
import ru.pioneersystem.pioneer2.dao.RouteDao;
import ru.pioneersystem.pioneer2.model.Event;
import ru.pioneersystem.pioneer2.model.Group;
import ru.pioneersystem.pioneer2.service.DictionaryService;
import ru.pioneersystem.pioneer2.service.EventService;
import ru.pioneersystem.pioneer2.service.GroupService;
import ru.pioneersystem.pioneer2.service.exception.RestrictionException;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.service.CurrentUser;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Service("groupService")
public class GroupServiceImpl implements GroupService {
    private EventService eventService;
    private GroupDao groupDao;
    private PartDao partDao;
    private RouteDao routeDao;
    private DictionaryService dictionaryService;
    private CurrentUser currentUser;
    private LocaleBean localeBean;
    private MessageSource messageSource;

    @Autowired
    public GroupServiceImpl(EventService eventService, GroupDao groupDao, PartDao partDao, RouteDao routeDao,
                            DictionaryService dictionaryService, CurrentUser currentUser, LocaleBean localeBean,
                            MessageSource messageSource) {
        this.eventService = eventService;
        this.groupDao = groupDao;
        this.partDao = partDao;
        this.routeDao = routeDao;
        this.dictionaryService = dictionaryService;
        this.currentUser = currentUser;
        this.localeBean = localeBean;
        this.messageSource = messageSource;
    }

    @Override
    public List<Group> getGroupList() throws ServiceException {
        try {
            List<Group> groups;
            if (currentUser.isSuperRole()) {
                groups = groupDao.getSuperList();
            } else {
                groups = groupDao.getAdminList(currentUser.getUser().getCompanyId());
            }

            for (Group group : groups) {
                String roleName = dictionaryService.getLocalizedRoleName(group.getRoleId(), localeBean.getLocale());
                if (roleName != null) {
                    group.setRoleName(roleName);
                }
            }
            return groups;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.group.NotLoadedList", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage());
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
            eventService.logError(mess, e.getMessage());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public Map<String, Integer> getUserPublishMap() throws ServiceException {
        try {
            return groupDao.getUserPublishGroup(currentUser.getUser().getId(), currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.group.userPublishGroupNotLoaded", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public Map<String, Integer> getUserCreateGroupsMap() throws ServiceException {
        try {
            return groupDao.getUserCreateGroup(currentUser.getUser().getId(), currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.group.userCreateGroupNotLoaded", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public Map<String, Integer> getForSearchGroupMap() throws ServiceException {
        try {
            if (currentUser.isAdminRole()) {
                return groupDao.getCreateGroup(currentUser.getUser().getCompanyId());
            } else {
                return groupDao.getUserCreateGroup(currentUser.getUser().getId(), currentUser.getUser().getCompanyId());
            }
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.group.userGroupNotLoaded", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public Map<Integer, Map<Integer, Integer>> getUserRolesGroupActivityMap() throws ServiceException {
        try {
            return groupDao.getUserRolesGroupActivity(currentUser.getUser().getId(), currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.group.userRolesGroupActivity", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public List<String> groupsWithRole(int roleId) throws ServiceException {
        try {
            return groupDao.groupsWithRole(roleId, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.group.groupsWithRole", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), roleId);
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public Group getNewGroup() {
        Group group = new Group();
        group.setLinkUsers(new ArrayList<>());
        group.setCreateFlag(true);
        return group;
    }

    @Override
    public Group getGroup(Group selectedGroup) throws ServiceException {
        if (selectedGroup == null) {
            String mess = messageSource.getMessage("error.group.NotSelected", null, localeBean.getLocale());
            throw new RestrictionException(mess);
        }

        try {
            int companyId;
            if (currentUser.isSuperRole()) {
                companyId = selectedGroup.getCompanyId();
            } else {
                companyId = currentUser.getUser().getCompanyId();
            }

            Group group = groupDao.get(selectedGroup.getId(), companyId);
            group.setCreateFlag(false);
            eventService.logEvent(Event.Type.GROUP_GETED, selectedGroup.getId());
            return group;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.group.NotLoaded", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), selectedGroup.getId());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    public void saveGroup(Group group) throws ServiceException {
        try {
            if (group.isCreateFlag()) {
                int groupId = groupDao.create(group, currentUser.getUser().getCompanyId());
                eventService.logEvent(Event.Type.GROUP_CREATED, groupId);
            } else {
                groupDao.update(group, currentUser.getUser().getCompanyId());
                eventService.logEvent(Event.Type.GROUP_CHANGED, group.getId());
            }
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.group.NotSaved", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), group.getId());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void deleteGroup(Group group) throws ServiceException {
        // TODO: 28.02.2017 Проверка на удаление системной группы плюс еще какая-нибудь проверка
        try {
            partDao.removeGroupRestriction(group.getId(), currentUser.getUser().getCompanyId());
            routeDao.removeGroupRestriction(group.getId(), currentUser.getUser().getCompanyId());
            groupDao.delete(group.getId(), currentUser.getUser().getCompanyId());
            eventService.logEvent(Event.Type.GROUP_DELETED, group.getId());
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.group.NotDeleted", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage(), group.getId());
            throw new ServiceException(mess, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int createGroupWithUser(String groupName, int roleId, int userId, int companyId) throws ServiceException {
        try {
            Group group = getNewGroup();
            group.setName(groupName);
            group.setRoleId(roleId);

            if (userId > 0) {
                Group.LinkUser linkUser = new Group.LinkUser();
                linkUser.setUserId(userId);
                linkUser.setParticipant(true);
                group.getLinkUsers().add(linkUser);
            }

            int groupId = groupDao.create(group, companyId);
            eventService.logEvent(Event.Type.GROUP_CREATED, groupId);
            return groupId;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.group.groupWithUserNotCreated", null, localeBean.getLocale());
            eventService.logError(mess, e.getMessage());
            throw new ServiceException(mess, e);
        }
    }
}
