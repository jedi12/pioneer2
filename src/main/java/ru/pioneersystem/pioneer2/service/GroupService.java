package ru.pioneersystem.pioneer2.service;

import ru.pioneersystem.pioneer2.model.Group;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import java.util.List;
import java.util.Map;

public interface GroupService {

    List<Group> getGroupList() throws ServiceException;

    Map<String, Integer> getGroupMap() throws ServiceException;

    Map<String, Group> getPointMap() throws ServiceException;

    Map<String, Integer> getUserPublishMap() throws ServiceException;

    Map<String, Integer> getUserCreateGroupsMap() throws ServiceException;

    Group getNewGroup();

    Group getGroup(int groupId) throws ServiceException;

    void saveGroup(Group role) throws ServiceException;

    void deleteGroup(int groupId) throws ServiceException;
}