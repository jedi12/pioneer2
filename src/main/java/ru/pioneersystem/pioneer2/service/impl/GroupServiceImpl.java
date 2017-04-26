package ru.pioneersystem.pioneer2.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.dao.GroupDao;
import ru.pioneersystem.pioneer2.model.Group;
import ru.pioneersystem.pioneer2.service.GroupService;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.view.CurrentUser;

import java.util.List;

@Service("groupService")
public class GroupServiceImpl implements GroupService {
    private Logger log = LoggerFactory.getLogger(GroupServiceImpl.class);

    private GroupDao groupDao;
    private CurrentUser currentUser;

    @Autowired
    public GroupServiceImpl(GroupDao groupDao, CurrentUser currentUser) {
        this.groupDao = groupDao;
        this.currentUser = currentUser;
    }

    @Override
    public Group getGroup(int id) throws ServiceException {
        try {
            return groupDao.get(id);
        } catch (DataAccessException e) {
            log.error("Can't get Group by id", e);
            throw new ServiceException("Can't get Group by id", e);
        }
    }

    @Override
    public List<Group> getGroupList() throws ServiceException {
        try {
            return groupDao.getList(currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            log.error("Can't get list of Group", e);
            throw new ServiceException("Can't get list of Group", e);
        }
    }

    @Override
    public void createGroup(Group group) throws ServiceException {
        try {
            groupDao.create(group, currentUser.getUser().getCompanyId());
        } catch (DataAccessException e) {
            log.error("Can't create Group", e);
            throw new ServiceException("Can't create Group", e);
        }
    }

    @Override
    public void updateGroup(Group group) throws ServiceException {
        try {
            groupDao.update(group);
        } catch (DataAccessException e) {
            log.error("Can't update Group", e);
            throw new ServiceException("Can't update Group", e);
        }
    }

    @Override
    public void deleteGroup(int id) throws ServiceException {
        // TODO: 28.02.2017 Проверка на удаление системной группы плюс еще какая-нибудь проверка
        // пример:
        // установить @Transactional(rollbackForClassName = DaoException.class)
        // после проверки выбрасывать RestrictException("Нельзя удалять, пока используется в шаблоне")
        // в ManagedBean проверять, если DaoException - то выдавать сообщение из DaoException
        try {
            groupDao.delete(id);
        } catch (DataAccessException e) {
            log.error("Can't delete Group", e);
            throw new ServiceException("Can't delete Group", e);
        }
    }
}