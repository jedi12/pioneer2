package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.dao.exception.RestrictException;
import ru.pioneersystem.pioneer2.model.Group;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;
import java.util.Map;

public interface GroupService {
    Group getGroup(int id) throws ServiceException;

    List<Group> getGroupList() throws ServiceException;

    Map<String, Integer> getGroupMap() throws ServiceException;

    Map<String, Group> getPointMap() throws ServiceException;

    Map<String, Integer> getUserPublishMap() throws ServiceException;

    Map<String, Integer> getUserCreateMap() throws ServiceException;

    void createGroup(Group role) throws ServiceException;

    void updateGroup(Group role) throws ServiceException;

    void deleteGroup(int id) throws ServiceException, RestrictException;
}